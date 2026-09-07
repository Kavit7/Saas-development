package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="support_tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportTicket extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="raised_by", nullable=false)
    private User raisedBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assigned_to")
    private User assignedTo;
    @Enumerated(EnumType.STRING) private SupportCategory category;
    private String subject;
    @Column(columnDefinition="TEXT") private String description;
    private String relatedEntityType;
    private UUID relatedEntityId;
    @Enumerated(EnumType.STRING) private Priority priority;
    @Enumerated(EnumType.STRING) private TicketStatus status;
    @Column(columnDefinition="TEXT") private String resolutionNotes;
    @Version private Integer version;
    private OffsetDateTime resolvedAt;
}