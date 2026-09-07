package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name="payment_links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLink extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invoice_id", nullable=false)
    private Invoice invoice;
    @Column(nullable=false, unique=true) private String token;
    private BigDecimal amount;
    private String currency;
    @Column(columnDefinition="jsonb") private String allowedMethods;
    private String status;
    private OffsetDateTime expiresAt;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="paid_payment_id")
    private Payment paidPayment;
}