package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;
    private String type;
    private String title;
    @Column(columnDefinition="TEXT") private String message;
    @Enumerated(EnumType.STRING) private Priority priority;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime sentAt;
    private OffsetDateTime readAt;
    private String status;
}