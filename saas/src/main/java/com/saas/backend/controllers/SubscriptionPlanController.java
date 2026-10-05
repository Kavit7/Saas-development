package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.SubscriptionRequest;
import com.saas.backend.response.SubscriptionResponse;
import com.saas.backend.serviceImpl.SubscriptionPlanServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/api/subscription-plans")
public class SubscriptionPlanController {
    private final SubscriptionPlanServiceImpl subscriptionPlanService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createSubscriptionPlan(@RequestBody SubscriptionRequest subscriptionRequest) {
        try {
            SubscriptionResponse response = subscriptionPlanService.createSubscriptionPlan(subscriptionRequest);
            return ResponseEntity.ok(Map.of("message", "Subscription plan created successfully", "data", response));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getAllSubscriptionPlans() {
        try {
            List<SubscriptionResponse> plans = subscriptionPlanService.getAllSubscriptionPlans();
            return ResponseEntity.ok(Map.of("message", "Subscription plans loaded successfully", "data", plans));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveSubscriptionPlans() {
        try {
            List<SubscriptionResponse> plans = subscriptionPlanService.getActiveSubscriptionPlans();
            return ResponseEntity.ok(Map.of("message", "Active subscription plans loaded successfully", "data", plans));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
