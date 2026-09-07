package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="knowledge_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeDocument extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="uploaded_by", nullable=false)
    private User uploadedBy;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    private String sourceType;
    private String sourceReference;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String storagePath;
    @Enumerated(EnumType.STRING) private KnowledgeStatus status;
    private Integer version;
    private java.time.OffsetDateTime publishedAt;
}