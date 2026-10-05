package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime createdAt;
}
