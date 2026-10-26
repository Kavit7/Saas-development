package com.saas.backend.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafariRescheduleRequest {
    @NotNull(message = "New start date is required")
    private LocalDate newStartDate;
    private String reason;
}
