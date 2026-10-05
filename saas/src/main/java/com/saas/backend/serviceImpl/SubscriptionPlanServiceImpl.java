package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.backend.dto.SubscriptionRequest;
import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.models.SubscriptionStatus;
import com.saas.backend.repositories.SubscriptionPlanRepository;
import com.saas.backend.response.SubscriptionResponse;
import com.saas.backend.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private SubscriptionResponse mapToResponse(SubscriptionPlan plan) {
        if (plan == null) return null;
        return SubscriptionResponse.builder()
                .id(plan.getId() != null ? plan.getId().toString() : null)
                .name(plan.getName())
                .price(plan.getPrice())
                .currency(plan.getCurrency() != null ? plan.getCurrency() : "USD")
                .maxUsers(plan.getMaxUsers())
                .status(plan.getStatus() != null ? plan.getStatus() : SubscriptionStatus.ACTIVE)
                .createdAt(plan.getCreatedAt())
                .build();
    }

    @Override
    public SubscriptionResponse createSubscriptionPlan(SubscriptionRequest subscriptionRequest) {
        try {
            if (subscriptionPlanRepository.findByNameIgnoreCase(subscriptionRequest.getName()).isPresent()) {
                throw new RuntimeException("Subscription plan with name '" + subscriptionRequest.getName() + "' already exists");
            }

            SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
            subscriptionPlan.setName(subscriptionRequest.getName());
            subscriptionPlan.setPrice(subscriptionRequest.getPrice());
            subscriptionPlan.setCurrency(subscriptionRequest.getCurrency() != null ? subscriptionRequest.getCurrency() : "USD");
            subscriptionPlan.setMaxUsers(subscriptionRequest.getMaxUsers());
            subscriptionPlan.setStatus(subscriptionRequest.getStatus() != null ? subscriptionRequest.getStatus() : SubscriptionStatus.ACTIVE);
            subscriptionPlanRepository.save(subscriptionPlan);

            return mapToResponse(subscriptionPlan);
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptionPlans() {
        return subscriptionPlanRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getActiveSubscriptionPlans() {
        return subscriptionPlanRepository.findAllByStatus(SubscriptionStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
}
