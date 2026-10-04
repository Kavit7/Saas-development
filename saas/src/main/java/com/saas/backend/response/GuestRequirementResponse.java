package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.RequirementType;
import com.saas.backend.models.Severity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestRequirementResponse {
    private UUID id;
    private UUID guestId;
    private String guestName;
    private RequirementType requirementType;
    private String requirementValue;
    private Severity severity;
    private String notes;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
