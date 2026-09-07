package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="property_tags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyTag extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id", nullable=false)
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="tag_id", nullable=false)
    private Tag tag;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
}