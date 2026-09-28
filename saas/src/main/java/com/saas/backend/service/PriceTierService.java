package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.PriceTierRequest;
import com.saas.backend.models.PriceTier;

public interface PriceTierService {


    PriceTier createPriceTier(PriceTierRequest request);
    void deletePriceTier(UUID priceId);
    List<PriceTier> getAllPriceTier();
    PriceTier updatPriceTier(UUID priceId,PriceTierRequest request);

    
}
