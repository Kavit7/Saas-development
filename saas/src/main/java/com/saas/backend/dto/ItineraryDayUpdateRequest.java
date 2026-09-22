package com.saas.backend.dto;

import java.util.UUID;

import lombok.Data;



@Data 
public class ItineraryDayUpdateRequest {
    UUID dayId;
    private String destination;
    private String notes;
}
