package com.saas.backend.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/platform-admin/subscription-plans")

public class SubscriptionPlanController {
    private final SubscriptionPlanServiceImpl subscriptionPlanService;
    
   @PostMapping("/create")
   public ResponseEntity<?> createSubscriptionPlan(@RequestBody SubscriptionRequest subscriptionRequest){
        try{
            SubscriptionResponse response = subscriptionPlanService.createSubscriptionPlan(subscriptionRequest);
            return ResponseEntity.ok(Map.of("message", "Subscription plan created successfully", "id", response.getId(), "name", response.getName(), "createdAt", response.getCreatedAt()));
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
