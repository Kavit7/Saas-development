package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="property_amenities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyAmenity extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id", nullable=false)
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="amenity_id", nullable=false)
    private Amenity amenity;
}