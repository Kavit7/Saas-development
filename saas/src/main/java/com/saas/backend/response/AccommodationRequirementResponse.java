package com.saas.backend.response;

import java.util.List;
import java.util.UUID;

import com.saas.backend.models.AccomodationRequirmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
@Builder
public class AccommodationRequirementResponse {

    private UUID id;
    private UUID safariId;
    private UUID itineraryDayId;
    private java.time.LocalDate itineraryDayDate;
    private Integer dayNumber;
    private String destination;
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
