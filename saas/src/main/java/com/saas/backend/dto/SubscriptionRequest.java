package com.saas.backend.dto;

import java.math.BigDecimal;


import com.saas.backend.models.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor 
public class SubscriptionRequest {
    private String name;
    private BigDecimal price;
    private String currency;
    private Integer maxUsers;
    private SubscriptionStatus status;
}
