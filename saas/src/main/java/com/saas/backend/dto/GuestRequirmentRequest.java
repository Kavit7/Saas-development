package com.saas.backend.dto;



import com.saas.backend.models.RequirementType;
import com.saas.backend.models.Severity;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data 
@AllArgsConstructor
public class GuestRequirmentRequest {
    private RequirementType requirementType;
    private String requirementValue;
    private Severity severity;
    private String notes;
}
