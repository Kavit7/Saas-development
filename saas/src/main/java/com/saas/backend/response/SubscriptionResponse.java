


package com.saas.backend.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class SubscriptionResponse {
     private String name;
     private String id;
     private OffsetDateTime createdAt;
}