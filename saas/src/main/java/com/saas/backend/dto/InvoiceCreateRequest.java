package com.saas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
public class InvoiceCreateRequest {
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    private String invoiceNumber;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String currency;

    private LocalDate dueDate;

    private InvoiceStatus status;

    private String fileName;

    private String filePath;
}
