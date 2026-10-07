package com.saas.backend.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private UUID bookingId;
    private String bookingReference;
    private String safariReference;
    private String propertyName;
    private String clientName;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private String fileName;
    private String filePath;
    private String fileType;
    private OffsetDateTime issuedAt;

    // Banking & Payment Plan Details (from JSONB)
    private String paymentDetailsJson;
    private com.saas.backend.dto.BankDetailsDto bankDetails;
    private java.util.List<com.saas.backend.dto.PaymentPlanItemDto> paymentPlan;
    private Double confidenceScore;
    private String extractionNotes;

    // Human-in-the-loop Verification
    private Boolean verified;
    private UUID verifiedById;
    private String verifiedByName;
    private OffsetDateTime verifiedAt;
    private String verificationNotes;

    // AI Detection status and manual fallback
    private Boolean aiExtracted;
    private Boolean needsManualReview;
    private String extractionError;
}
