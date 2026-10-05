package com.saas.backend.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    
}
