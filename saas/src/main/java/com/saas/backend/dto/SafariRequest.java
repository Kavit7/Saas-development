package com.saas.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.backend.models.SafariStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data 
@RequiredArgsConstructor 
@AllArgsConstructor 
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafariRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPassengers;
    private String notes;
    private SafariStatus status;
    private UUID salesPersonId;
}
