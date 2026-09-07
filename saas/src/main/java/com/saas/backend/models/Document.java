package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name="documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="uploaded_by")
    private User uploadedBy;
    private String entityType;
    private UUID entityId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String storagePath;
    private String documentType;
}