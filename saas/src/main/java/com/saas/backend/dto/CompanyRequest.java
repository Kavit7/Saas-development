package com.saas.backend.dto;


import com.saas.backend.models.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    private String name;
    private String email;
    private String phone;  
    private String country; 
    private String timezone;
    private String subscription_plan;
    private SubscriptionStatus status;

    
}
