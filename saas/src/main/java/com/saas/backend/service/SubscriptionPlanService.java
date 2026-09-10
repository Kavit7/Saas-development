package com.saas.backend.service;

import com.saas.backend.dto.SubscriptionRequest;
import com.saas.backend.response.SubscriptionResponse;

public interface SubscriptionPlanService {

    public SubscriptionResponse createSubscriptionPlan(SubscriptionRequest subscriptionRequest);
    
}
