package com.saas.backend.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.OccasionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialOccasionResponse {
    private UUID id;
    private UUID clientId;
    private UUID guestId;
    private String guestName;
    private OccasionType occasionType;
    private LocalDate occasionDate;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
