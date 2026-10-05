


package com.saas.backend.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.saas.backend.models.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
     private String id;
     private String name;
     private BigDecimal price;
     private String currency;
     private Integer maxUsers;
     private SubscriptionStatus status;
     private OffsetDateTime createdAt;

     public SubscriptionResponse(String name, String id, OffsetDateTime createdAt) {
         this.name = name;
         this.id = id;
         this.createdAt = createdAt;
     }
}