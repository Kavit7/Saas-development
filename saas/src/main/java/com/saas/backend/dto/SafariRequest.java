package com.saas.backend.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data 
@RequiredArgsConstructor 
@AllArgsConstructor 
public class SafariRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPassengers;
    private String notes;
    
}
