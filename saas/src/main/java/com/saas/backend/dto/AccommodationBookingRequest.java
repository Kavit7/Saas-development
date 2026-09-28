package com.saas.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class AccommodationBookingRequest {
    private UUID accomodationRequirementId;
    private UUID propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String notes;
}
