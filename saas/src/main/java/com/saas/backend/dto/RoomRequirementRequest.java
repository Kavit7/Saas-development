package com.saas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class RoomRequirementRequest {
    private String roomType;
    private Integer quantity;
    
}
