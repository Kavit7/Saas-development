package com.saas.backend.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@AllArgsConstructor 
@NoArgsConstructor
@Builder
public class RequirementRequest {
    
    private UUID categoryId;
    private UUID pricetierId;
    private Integer numberOfrooms;
    List<RoomRequirementRequest> roomRequirements;
    private String roomPreferences;
    private String specialRequests;
        
}
