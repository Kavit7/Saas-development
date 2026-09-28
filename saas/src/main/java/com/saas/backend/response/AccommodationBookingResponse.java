package com.saas.backend.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data 
@AllArgsConstructor 

public class AccommodationBookingResponse {
    private UUID id;
    private String referenceNumber;
    private UUID requirementId;
    private String propertyName;
    private String reservationManagerName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BookingStatus status;
    private String confirmationNumber;
    private OffsetDateTime requestedAt;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime respondedAt;
    private String notes;
}
