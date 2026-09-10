package com.saas.backend.serviceImpl;

import org.springframework.stereotype.Service;

import com.saas.backend.dto.SubscriptionRequest;
import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.repositories.SubscriptionPlanRepository;
import com.saas.backend.response.SubscriptionResponse;
import com.saas.backend.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

  private final SubscriptionPlanRepository subscriptionPlanRepository;
     public SubscriptionResponse createSubscriptionPlan(SubscriptionRequest subscriptionRequest) {
        

        try{

        
            SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
            subscriptionPlan.setName(subscriptionRequest.getName());
            subscriptionPlan.setPrice(subscriptionRequest.getPrice());
            subscriptionPlan.setCurrency(subscriptionRequest.getCurrency());
            subscriptionPlan.setMaxUsers(subscriptionRequest.getMaxUsers());
            subscriptionPlan.setStatus(subscriptionRequest.getStatus());
            // save subscription plan to database
            subscriptionPlanRepository.save(subscriptionPlan);
            return new SubscriptionResponse(subscriptionPlan.getName(), subscriptionPlan.getId().toString(), subscriptionPlan.getCreatedAt());
        }
        catch(Exception e){
            throw new RuntimeException("Error creating subscription plan: " + e.getMessage());
        }

    }
    
}
