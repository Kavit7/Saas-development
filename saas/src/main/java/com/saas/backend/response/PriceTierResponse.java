package com.saas.backend.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTierResponse {
    private UUID id;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
