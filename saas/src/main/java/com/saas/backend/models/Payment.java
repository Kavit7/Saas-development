package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name="payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private Client client;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    private BigDecimal amount;
    private String currency;
    @Column(unique=true) private String paymentReference;
    private OffsetDateTime paymentDate;
    private String proofNumber;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private GatewayProvider gatewayProvider;
    private String gatewayTransactionId;
    @Enumerated(EnumType.STRING) private GatewayStatus gatewayStatus;
    @Column(unique=true) private String idempotencyKey;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recorded_by")
    private User recordedBy;
    @Column(columnDefinition="TEXT") private String notes;
    @Version private Integer version;
}