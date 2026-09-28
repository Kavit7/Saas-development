package com.saas.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoomType
 */
@Entity 
@Table (name="room_type")
@AllArgsConstructor 
@NoArgsConstructor 
@Data
@Builder 
public class RoomType extends BaseEntity {
    @Column(name="name", nullable=false)
    private String name;

}
