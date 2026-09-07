package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="ai_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiFeedback extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ai_interaction_id", nullable=false)
    private AiInteraction aiInteraction;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
    private User user;
    private Integer rating;
    @Enumerated(EnumType.STRING) private FeedbackType feedbackType;
    private Boolean correct;
    @Column(columnDefinition="TEXT") private String comment;
}