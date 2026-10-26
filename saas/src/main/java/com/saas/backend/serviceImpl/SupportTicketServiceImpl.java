package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.dto.SupportTicketRequest;
import com.saas.backend.dto.SupportTicketUpdateRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.Priority;
import com.saas.backend.models.SupportCategory;
import com.saas.backend.models.SupportTicket;
import com.saas.backend.models.TicketStatus;
import com.saas.backend.models.User;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.SupportTicketRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.SupportTicketResponse;
import com.saas.backend.service.AuditLogService;
import com.saas.backend.service.SupportTicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    private SupportTicketResponse mapToResponse(SupportTicket ticket) {
        if (ticket == null) return null;
        String raisedByName = ticket.getRaisedBy() != null
                ? (ticket.getRaisedBy().getFirstName() + " " + ticket.getRaisedBy().getLastName()).trim()
                : "Unknown";
        String raisedByEmail = ticket.getRaisedBy() != null ? ticket.getRaisedBy().getEmail() : null;

        String assignedToName = ticket.getAssignedTo() != null
                ? (ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName()).trim()
                : null;
        String assignedToEmail = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : null;

        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .companyId(ticket.getCompany() != null ? ticket.getCompany().getId() : null)
                .companyName(ticket.getCompany() != null ? ticket.getCompany().getName() : null)
                .raisedById(ticket.getRaisedBy() != null ? ticket.getRaisedBy().getId() : null)
                .raisedByName(raisedByName)
                .raisedByEmail(raisedByEmail)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(assignedToName)
                .assignedToEmail(assignedToEmail)
                .category(ticket.getCategory())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .relatedEntityType(ticket.getRelatedEntityType())
                .relatedEntityId(ticket.getRelatedEntityId())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .resolutionNotes(ticket.getResolutionNotes())
                .version(ticket.getVersion())
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public SupportTicketResponse createTicket(SupportTicketRequest request, Authentication auth) {
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        if (isSuperAdmin) {
            throw new AccessDeniedException("Super Admin does not raise direct support tickets. General tickets must be raised by tenant users, or generated automatically via Account Personalization.");
        }

        User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                ? userRepository.findByEmail(auth.getName()).orElse(null)
                : null;

        if (currentUser == null) {
            throw new AccessDeniedException("User must be authenticated to create a support ticket.");
        }

        Company company = currentUser.getCompany();
        if (company == null) {
            throw new AccessDeniedException("User has no associated company to file a ticket under");
        }

        User raisedBy = currentUser;
        if (raisedBy == null) {
            raisedBy = userRepository.findByCompany(company, PageRequest.of(0, 1)).getContent().stream().findFirst().orElse(null);
        }
        if (raisedBy == null) {
            throw new RuntimeException("Cannot create ticket: Company has no registered users to attach ticket to.");
        }

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId()).orElse(null);
        }

        SupportTicket ticket = SupportTicket.builder()
                .company(company)
                .raisedBy(raisedBy)
                .assignedTo(assignedTo)
                .category(request.getCategory() != null ? request.getCategory() : SupportCategory.OTHER)
                .subject(request.getSubject())
                .description(request.getDescription())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TicketStatus.OPEN)
                .resolutionNotes(request.getResolutionNotes())
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);

        try {
            auditLogService.recordAuditLog(
                    company,
                    currentUser,
                    "CREATE_SUPPORT_TICKET",
                    "support_ticket",
                    saved.getId(),
                    null,
                    "{\"subject\":\"" + (saved.getSubject() != null ? saved.getSubject().replace("\"", "'") : "") + "\"}",
                    "0.0.0.0",
                    "Internal Web Client"
            );
        } catch (Exception ex) {
            log.warn("Audit log error on ticket create: {}", ex.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTickets(Authentication auth, int page, int size, String status) {
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (isSuperAdmin) {
            return supportTicketRepository.findAll(pageable).map(this::mapToResponse);
        }

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        if (currentUser.getCompany() == null) {
            throw new AccessDeniedException("User has no associated company");
        }

        return supportTicketRepository.findByCompany_Id(currentUser.getCompany().getId(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(UUID id, Authentication auth) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support ticket not found"));

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        if (!isSuperAdmin) {
            User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            if (currentUser == null || currentUser.getCompany() == null ||
                    !currentUser.getCompany().getId().equals(ticket.getCompany().getId())) {
                throw new AccessDeniedException("Access denied to support ticket of another company");
            }
        }

        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public SupportTicketResponse updateTicket(UUID id, SupportTicketUpdateRequest request, Authentication auth) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support ticket not found"));

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                ? userRepository.findByEmail(auth.getName()).orElse(null)
                : null;

        if (!isSuperAdmin) {
            if (currentUser == null || currentUser.getCompany() == null ||
                    !currentUser.getCompany().getId().equals(ticket.getCompany().getId())) {
                throw new AccessDeniedException("Access denied to update support ticket of another company");
            }
        }

        String oldValues = "{\"status\":\"" + ticket.getStatus() + "\",\"priority\":\"" + ticket.getPriority() + "\"}";

        if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
            ticket.setSubject(request.getSubject().trim());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            ticket.setDescription(request.getDescription().trim());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED) {
                ticket.setResolvedAt(OffsetDateTime.now());
            }
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getAssignedToId() != null) {
            User assigned = userRepository.findById(request.getAssignedToId()).orElse(null);
            if (assigned != null) {
                ticket.setAssignedTo(assigned);
            }
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes());
        }

        SupportTicket saved = supportTicketRepository.save(ticket);

        String newValues = "{\"status\":\"" + saved.getStatus() + "\",\"priority\":\"" + saved.getPriority() + "\"}";

        try {
            auditLogService.recordAuditLog(
                    saved.getCompany(),
                    currentUser,
                    "UPDATE_SUPPORT_TICKET",
                    "support_ticket",
                    saved.getId(),
                    oldValues,
                    newValues,
                    "0.0.0.0",
                    "Internal Web Client"
            );
        } catch (Exception ex) {
            log.warn("Audit log error on ticket update: {}", ex.getMessage());
        }

        return mapToResponse(saved);
    }
}
