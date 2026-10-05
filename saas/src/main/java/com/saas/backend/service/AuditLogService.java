package com.saas.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.models.Company;
import com.saas.backend.models.User;
import com.saas.backend.response.AuditLogResponse;

public interface AuditLogService {

    Page<AuditLogResponse> getAuditLogs(Authentication auth, int page, int size, String action, String entityType);

    void recordAuditLog(Company company, User user, String action, String entityType, UUID entityId, String oldValues, String newValues, String ipAddress, String userAgent);
}
