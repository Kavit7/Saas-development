package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

@Entity @Table(name="subscription_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionPlan extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    private BigDecimal price;
    private String currency;
    private Integer maxUsers;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") private JsonNode features;
    @Enumerated(EnumType.STRING) private SubscriptionStatus status;
    @Version private Integer version;
}