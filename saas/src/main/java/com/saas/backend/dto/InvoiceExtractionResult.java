package com.saas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceExtractionResult {
    private boolean success;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private BankDetailsDto bankDetails;
    private List<PaymentPlanItemDto> paymentPlan;
    private Double confidenceScore;
    private String extractionNotes;
    private String errorMessage;
    private String rawResponse;
}
