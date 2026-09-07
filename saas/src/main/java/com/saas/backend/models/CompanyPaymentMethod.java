package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="company_payment_methods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyPaymentMethod extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @Enumerated(EnumType.STRING) private GatewayProvider gatewayProvider;
    @Enumerated(EnumType.STRING) private CompanyPaymentMethodType methodType;
    @Column(nullable=false) private String gatewayToken;
    private String displayLabel;
    private Boolean isDefault;
    @Enumerated(EnumType.STRING) private CompanyPaymentMethodStatus status;
}