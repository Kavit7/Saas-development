package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="subscription_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionPlan extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    private BigDecimal price;
    private String currency;
    private Integer maxUsers;
    @Column(columnDefinition="jsonb") private String features;
    @Enumerated(EnumType.STRING) private SubscriptionStatus status;
    @Version private Integer version;
}