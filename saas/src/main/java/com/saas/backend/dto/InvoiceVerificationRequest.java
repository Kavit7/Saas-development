package com.saas.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceVerificationRequest {
    @NotNull(message = "Verification approval status is required")
    private Boolean verified;
    private String verificationNotes;
}
