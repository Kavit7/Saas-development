package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name="notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Note extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;
    private String entityType;
    private UUID entityId;
    @Column(nullable=false, columnDefinition="TEXT") private String content;
}