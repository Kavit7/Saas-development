package com.saas.backend.serviceImpl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.models.AuditLog;
import com.saas.backend.models.Company;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AuditLogRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AuditLogResponse;
import com.saas.backend.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private AuditLogResponse mapToResponse(AuditLog log) {
        if (log == null) return null;
        String userName = log.getUser() != null
                ? (log.getUser().getFirstName() + " " + log.getUser().getLastName()).trim()
                : "System";
        String userEmail = log.getUser() != null ? log.getUser().getEmail() : "system@platform.internal";
        String companyName = log.getCompany() != null ? log.getCompany().getName() : "Platform";

        return AuditLogResponse.builder()
                .id(log.getId())
                .companyId(log.getCompany() != null ? log.getCompany().getId() : null)
                .companyName(companyName)
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(userName)
                .userEmail(userEmail)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValues(log.getOldValues())
                .newValues(log.getNewValues())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Authentication auth, int page, int size, String action, String entityType) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthorized: Authentication context is missing");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (isSuperAdmin) {
            return auditLogRepository.findAll(pageable).map(this::mapToResponse);
        }

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User account not found"));

        if (currentUser.getCompany() == null) {
            throw new AccessDeniedException("User has no associated company");
        }

        return auditLogRepository.findByCompany_Id(currentUser.getCompany().getId(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void recordAuditLog(Company company, User user, String action, String entityType, UUID entityId, String oldValues, String newValues, String ipAddress, String userAgent) {
        try {
            AuditLog log = AuditLog.builder()
                    .company(company)
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Audit logging must be non-blocking for business transactions, but logged as error
            log.error("Failed to record audit log for action {}: {}", action, e.getMessage());
        }
    }
}
