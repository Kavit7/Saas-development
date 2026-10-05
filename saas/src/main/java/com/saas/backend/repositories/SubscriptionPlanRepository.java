package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.models.SubscriptionStatus;

@Repository 
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
    SubscriptionPlan findSubscriptionPlanById(UUID id);
    List<SubscriptionPlan> findAllByStatus(SubscriptionStatus status);
    List<SubscriptionPlan> findAllByOrderByCreatedAtDesc();
}
