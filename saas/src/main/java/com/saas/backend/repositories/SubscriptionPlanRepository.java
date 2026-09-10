package com.saas.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.SubscriptionPlan;



@Repository 
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    
   Optional<SubscriptionPlan> findByName(String name);
   SubscriptionPlan findSubscriptionPlanById(UUID id);
}
