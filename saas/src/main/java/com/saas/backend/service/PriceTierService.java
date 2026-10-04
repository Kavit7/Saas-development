package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.PriceTierRequest;
import com.saas.backend.response.PriceTierResponse;

public interface PriceTierService {

    PriceTierResponse createPriceTier(PriceTierRequest request);
    void deletePriceTier(UUID priceId);
    List<PriceTierResponse> getAllPriceTier();
    PriceTierResponse updatPriceTier(UUID priceId, PriceTierRequest request);

}
