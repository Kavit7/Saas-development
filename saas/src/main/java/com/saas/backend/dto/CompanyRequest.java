package com.saas.backend.dto;

import java.util.UUID;

import com.saas.backend.models.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data 
@AllArgsConstructor

public class CompanyRequest {

    private String name;
    private String slug;
    private String email;
    private String phone;  
    private String country; 
    private String timezone;
    private UUID subscription_plan_id;
    private SubscriptionStatus status;

    
}
