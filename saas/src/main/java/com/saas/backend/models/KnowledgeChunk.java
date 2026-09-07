package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="knowledge_chunks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeChunk extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="knowledge_document_id", nullable=false)
    private KnowledgeDocument knowledgeDocument;
    private Integer chunkIndex;
    @Column(nullable=false, columnDefinition="TEXT") private String content;
    private Integer tokenCount;
    @Column(columnDefinition="jsonb") private String metadata;
}