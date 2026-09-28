package com.saas.backend.models;



import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoomRequirement
 */

@Entity 
@Table(name="room_requirements")
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class RoomRequirement extends BaseEntity {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="accommodation_requirment_id")
    @JsonManagedReference
    AccommodationRequirement accommodationRequirement;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_type_id")
    private RoomType roomType;
    @Column(name="quantity",nullable = false)
    private Integer quantity;
}
