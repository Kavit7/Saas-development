package com.saas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnpaidAccommodationAlertDto {
    private UUID bookingId;
    private String bookingReference;
    private String clientName;
    private String clientEmail;
    private String safariReference;
    private LocalDate safariStartDate;
    private String propertyName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private String urgency; // "OVERDUE", "DUE_IN_1_DAY", "DUE_IN_3_DAYS", "UPCOMING_SAFARI"
    private String statusDescription; // "OVERDUE INVOICE", "INVOICE DUE IN 3 DAYS", "AWAITING LODGE INVOICE"
    private Long daysRemaining; // positive = days left, 0 = today, negative = days overdue
    private boolean hasInvoice;
}
