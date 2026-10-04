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
    private OffsetDateTime issuedAt;
}
