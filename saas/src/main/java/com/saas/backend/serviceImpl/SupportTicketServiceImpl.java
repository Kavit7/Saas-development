package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import com.saas.backend.specification.SupportTicketSpecification;

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
                .targetRole(ticket.getTargetRole())
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

    private String resolveTargetRole(SupportCategory category, String requestedTargetRole) {
        if (requestedTargetRole != null && !requestedTargetRole.trim().isEmpty()) {
            return requestedTargetRole.trim().toUpperCase().replace("ROLE_", "");
        }
        if (category == null) return "ADMIN";
        switch (category) {
            case BOOKING_ISSUE:
                return "RESERVATION_MANAGER";
            case TECHNICAL:
                return "PLATFORM_ADMIN";
            case PAYMENT_ISSUE:
            case ACCOUNT_ISSUE:
            case LOGIN_ISSUE:
            case OTHER:
            default:
                return "ADMIN";
        }
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

        SupportCategory category = request.getCategory() != null ? request.getCategory() : SupportCategory.OTHER;
        String targetRole = resolveTargetRole(category, request.getTargetRole());

        SupportTicket ticket = SupportTicket.builder()
                .company(company)
                .raisedBy(raisedBy)
                .assignedTo(assignedTo)
                .targetRole(targetRole)
                .category(category)
                .subject(request.getSubject())
                .description(request.getDescription())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(TicketStatus.OPEN)
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

        TicketStatus ticketStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                ticketStatus = TicketStatus.valueOf(status.trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        if (isSuperAdmin) {
            // Super Admin ONLY sees Platform/Technical escalation tickets across SaaS
            Specification<SupportTicket> spec = SupportTicketSpecification.isPlatformTicket();
            if (ticketStatus != null) {
                spec = spec.and(SupportTicketSpecification.hasStatus(ticketStatus));
            }
            return supportTicketRepository.findAll(spec, pageable).map(this::mapToResponse);
        }

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        if (currentUser.getCompany() == null) {
            throw new AccessDeniedException("User has no associated company");
        }

        UUID companyId = currentUser.getCompany().getId();
        String userRole = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        String roleNorm = userRole.toUpperCase().replace("ROLE_", "").trim();

        Specification<SupportTicket> spec;
        if ("ADMIN".equals(roleNorm) || "COMPANY_ADMIN".equals(roleNorm) || "SUPER_ADMIN".equals(roleNorm)) {
            // Company Admin sees all tickets within their company
            spec = SupportTicketSpecification.isVisibleForCompanyAdmin(companyId);
        } else {
            // Regular Staff (Sales, Reservation Manager, Guide):
            // ONLY sees tickets raised by them, assigned to them, or queued for their targetRole within their company
            spec = SupportTicketSpecification.isVisibleForStaff(companyId, currentUser.getId(), roleNorm);
        }

        if (ticketStatus != null) {
            spec = spec.and(SupportTicketSpecification.hasStatus(ticketStatus));
        }

        return supportTicketRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(UUID id, Authentication auth) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support ticket not found"));

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        if (isSuperAdmin) {
            boolean isPlatform = "PLATFORM_ADMIN".equalsIgnoreCase(ticket.getTargetRole()) ||
                    ticket.getCategory() == SupportCategory.TECHNICAL;
            if (!isPlatform) {
                throw new AccessDeniedException("Access denied: Platform Admin can only inspect platform technical escalation tickets.");
            }
            return mapToResponse(ticket);
        }

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        if (currentUser.getCompany() == null || !currentUser.getCompany().getId().equals(ticket.getCompany().getId())) {
            throw new AccessDeniedException("Access denied to support ticket of another company");
        }

        String userRole = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        String roleNorm = userRole.toUpperCase().replace("ROLE_", "").trim();

        boolean isCompanyAdmin = "ADMIN".equals(roleNorm) || "COMPANY_ADMIN".equals(roleNorm);
        if (!isCompanyAdmin) {
            boolean isRaisedByMe = ticket.getRaisedBy() != null && ticket.getRaisedBy().getId().equals(currentUser.getId());
            boolean isAssignedToMe = ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(currentUser.getId());
            boolean isForMyRole = ticket.getTargetRole() != null && ticket.getTargetRole().equalsIgnoreCase(roleNorm);
            boolean isLegacyMatch = ticket.getTargetRole() == null && (
                ("RESERVATION_MANAGER".equalsIgnoreCase(roleNorm) && ticket.getCategory() == SupportCategory.BOOKING_ISSUE)
            );

            if (!isRaisedByMe && !isAssignedToMe && !isForMyRole && !isLegacyMatch) {
                throw new AccessDeniedException("Access denied: You do not have permission to view this ticket.");
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

        if (isSuperAdmin) {
            boolean isPlatform = "PLATFORM_ADMIN".equalsIgnoreCase(ticket.getTargetRole()) ||
                    ticket.getCategory() == SupportCategory.TECHNICAL;
            if (!isPlatform) {
                throw new AccessDeniedException("Access denied: Platform Admin can only update platform technical escalation tickets.");
            }
        } else {
            if (currentUser == null || currentUser.getCompany() == null ||
                    !currentUser.getCompany().getId().equals(ticket.getCompany().getId())) {
                throw new AccessDeniedException("Access denied to update support ticket of another company");
            }
        }

        String userRole = currentUser != null && currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        String roleNorm = userRole.toUpperCase().replace("ROLE_", "").trim();
        boolean isCompanyAdmin = "ADMIN".equals(roleNorm) || "COMPANY_ADMIN".equals(roleNorm);
        boolean isDesignatedRole = ticket.getTargetRole() != null && ticket.getTargetRole().equalsIgnoreCase(roleNorm);
        boolean isAlreadyAssigned = ticket.getAssignedTo() != null && currentUser != null && ticket.getAssignedTo().getId().equals(currentUser.getId());

        // Status change / resolution authorization
        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            boolean canChangeStatus = isSuperAdmin || isCompanyAdmin || isDesignatedRole || isAlreadyAssigned;
            if (!canChangeStatus) {
                throw new AccessDeniedException("Forbidden: Only designated department resolvers or administrators can change ticket status.");
            }
        }

        // Resolution notes authorization
        if (request.getResolutionNotes() != null && !request.getResolutionNotes().trim().isEmpty()) {
            boolean canResolve = isSuperAdmin || isCompanyAdmin || isDesignatedRole || isAlreadyAssigned;
            if (!canResolve) {
                throw new AccessDeniedException("Forbidden: Only designated department resolvers or administrators can submit resolution notes.");
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
            if (request.getTargetRole() == null || request.getTargetRole().trim().isEmpty()) {
                ticket.setTargetRole(resolveTargetRole(request.getCategory(), null));
            }
        }
        if (request.getTargetRole() != null && !request.getTargetRole().trim().isEmpty()) {
            if (isSuperAdmin || isCompanyAdmin) {
                ticket.setTargetRole(request.getTargetRole().trim().toUpperCase().replace("ROLE_", ""));
            }
        }

        // Auto-capture assignedTo resolver when ticket is in progress or resolved
        if (request.getStatus() == TicketStatus.IN_PROGRESS || request.getStatus() == TicketStatus.RESOLVED) {
            if (currentUser != null && (ticket.getAssignedTo() == null || isCompanyAdmin || isDesignatedRole)) {
                ticket.setAssignedTo(currentUser);
            }
        }

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED) {
                ticket.setResolvedAt(OffsetDateTime.now());
                if (currentUser != null && ticket.getAssignedTo() == null) {
                    ticket.setAssignedTo(currentUser);
                }
            }
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getAssignedToId() != null && (isSuperAdmin || isCompanyAdmin)) {
            User assigned = userRepository.findById(request.getAssignedToId()).orElse(null);
            if (assigned != null) {
                ticket.setAssignedTo(assigned);
            }
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes().trim());
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
