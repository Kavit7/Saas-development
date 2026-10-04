package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class CompanyResponse {
    private String id;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private String country;
    private String timezone;
    private UUID subscriptionPlanId;
    private String subscriptionPlanName;
    private SubscriptionStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public CompanyResponse(String id, String slug, OffsetDateTime createdAt) {
        this.id = id;
        this.slug = slug;
        this.createdAt = createdAt;
    }
}
