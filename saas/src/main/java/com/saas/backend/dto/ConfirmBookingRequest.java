package com.saas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ConfirmBookingRequest DTO
 * Transmits the official confirmation number returned by the property manager.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingRequest {
    @NotBlank(message = "Confirmation number is required")
    private String confirmationNumber;
}
