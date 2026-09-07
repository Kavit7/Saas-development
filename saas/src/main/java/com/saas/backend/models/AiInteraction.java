package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="ai_interactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiInteraction extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
    private User user;
    private String sessionId;
    @Column(nullable=false, columnDefinition="TEXT") private String question;
    @Column(columnDefinition="TEXT") private String response;
    private String intent;
    @Column(columnDefinition="jsonb") private String context;
    private String model;
    private String promptVersion;
}