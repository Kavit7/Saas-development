package com.saas.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name="subscription_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionPayment extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="subscription_invoice_id", nullable=false)
    private SubscriptionInvoice subscriptionInvoice;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING) private SubscriptionPaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private GatewayProvider gatewayProvider;
    private String gatewayTransactionId;
    @Enumerated(EnumType.STRING) private GatewayStatus gatewayStatus;
    @Column(unique=true) private String idempotencyKey;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recorded_by")
    private PlatformAdmin recordedBy;
    private OffsetDateTime paidAt;
    @Version private Integer version;
}
