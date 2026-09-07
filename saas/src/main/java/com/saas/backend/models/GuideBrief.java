package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="guide_briefs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuideBrief extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="guide_id", nullable=false)
    private User guide;
    private String title;
    private String status;
    private String filePath;
    private String shareToken;
    private java.time.OffsetDateTime generatedAt;
    private java.time.OffsetDateTime sentAt;
}