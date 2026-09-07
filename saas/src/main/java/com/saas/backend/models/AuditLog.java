package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name="audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
    private User user;
    private String action;
    private String entityType;
    private UUID entityId;
    @Column(columnDefinition="jsonb") private String oldValues;
    @Column(columnDefinition="jsonb") private String newValues;
    private String ipAddress;
    private String userAgent;
}