package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="company_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanySubscription extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="plan_id", nullable=false)
    private SubscriptionPlan plan;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private SubscriptionStatus status;
    private String externalSubscriptionId;
    private Boolean autoRenew;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="default_payment_method_id")
    private CompanyPaymentMethod defaultPaymentMethod;
    @Version private Integer version;
}