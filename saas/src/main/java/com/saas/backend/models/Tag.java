package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    @Enumerated(EnumType.STRING) private TagType tagType;
    @Column(columnDefinition="TEXT") private String description;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
}