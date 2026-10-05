package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.PriceTier;



@Repository 
public interface PriceTierRepository extends JpaRepository<PriceTier,UUID> {
    
    boolean existsByNameAndCurrencyIgnoreCase(String name,String currency);
    java.util.Optional<PriceTier> findByNameIgnoreCase(String name);
}
