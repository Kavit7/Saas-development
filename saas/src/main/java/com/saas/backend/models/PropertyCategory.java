package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="property_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyCategory extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    @Column(columnDefinition="TEXT") private String description;
}