package com.saas.backend.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.SafariStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafariResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private UUID salesPersonId;
    private String salesPersonName;
    private String referenceNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPassengers;
    private SafariStatus status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
