package com.saas.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="subscription_invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionInvoice extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_subscription_id", nullable=false)
    private CompanySubscription companySubscription;
    @Column(nullable=false, unique=true) private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private SubscriptionInvoiceStatus status;
    @Version private Integer version;
}
