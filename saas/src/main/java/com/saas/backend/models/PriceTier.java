package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="price_tiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceTier extends BaseEntity {
    @Column(nullable=false, unique=true) private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String currency;
    @Version private Integer version;
}