package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="payment_allocations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentAllocation extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="payment_id", nullable=false)
    private Payment payment;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invoice_id", nullable=false)
    private Invoice invoice;
    private BigDecimal allocatedAmount;
}