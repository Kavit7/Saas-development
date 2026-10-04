package com.saas.backend.dto;

import com.saas.backend.models.InvoiceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private InvoiceStatus status;
}
