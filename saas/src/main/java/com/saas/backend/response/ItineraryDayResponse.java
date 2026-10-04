package com.saas.backend.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDayResponse {
    private UUID id;
    private UUID safariId;
    private Integer dayNumber;
    private LocalDate date;
    private String destination;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
