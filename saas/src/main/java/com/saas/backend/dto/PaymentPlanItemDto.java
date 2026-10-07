package com.saas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlanItemDto {
    private String milestone;
    private BigDecimal percentage;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;
    private String notes;

}

