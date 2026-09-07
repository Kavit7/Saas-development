package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="amenities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Amenity extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    @Column(columnDefinition="TEXT") private String description;
}