package com.saas.backend.dto;

import java.time.LocalDate;


import com.saas.backend.models.OccasionType;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor 
@Data 
public class OccasionRequest {
    private OccasionType occasionType;
    private LocalDate occassionDate;
    private String notes;
}
