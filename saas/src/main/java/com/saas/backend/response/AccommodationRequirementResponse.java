package com.saas.backend.response;

import java.util.List;
import java.util.UUID;

import com.saas.backend.models.AccomodationRequirmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data 
@AllArgsConstructor 
public class AccommodationRequirementResponse {

    private UUID categoryId;
    private String categoryName;
    private UUID pricetierId;
    private String priceTierName;
    private Integer numberOfrooms;
    List<RoomRequirementResponse> roomRequirements;
    private String roomPreferences;
    private String specialRequests;
    private AccomodationRequirmentStatus status;
}
