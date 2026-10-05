package com.saas.backend.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ItineraryRequest DTO
 * Transmits day destination, date, and activity notes for an itinerary day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItineraryRequest {
    private String destination;
    private String notes;
    private LocalDate date;
}
