package com.saas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.saas.backend.models.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceManualUpdateRequest {
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BankDetailsDto bankDetails;
    private List<PaymentPlanItemDto> paymentPlan;
    private String notes;
}
