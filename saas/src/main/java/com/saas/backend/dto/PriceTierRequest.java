package com.saas.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class PriceTierRequest {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String currency;
}
