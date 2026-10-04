package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.FlightType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDetailResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private FlightType flightType;
    private String airline;
    private String flightNumber;
    private String airport;
    private OffsetDateTime arrivalDatetime;
    private OffsetDateTime departureDatetime;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
