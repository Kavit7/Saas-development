package com.saas.backend.service;

import java.util.List;

import com.saas.backend.dto.SubscriptionRequest;
import com.saas.backend.response.SubscriptionResponse;

public interface SubscriptionPlanService {
    SubscriptionResponse createSubscriptionPlan(SubscriptionRequest subscriptionRequest);
    List<SubscriptionResponse> getAllSubscriptionPlans();
    List<SubscriptionResponse> getActiveSubscriptionPlans();
}
