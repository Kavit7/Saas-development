package com.saas.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentDetailsDto {
    private BankDetailsDto bankDetails;
    private List<PaymentPlanItemDto> paymentPlan;
    private Double confidenceScore;
    private String detectionSource;
    private OffsetDateTime detectedAt;
    private String extractionNotes;
    private String rawSummary;
}
