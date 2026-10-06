package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.AmenityRequest;
import com.saas.backend.response.AmenityResponse;

public interface AmenityService {
    AmenityResponse createAmenity(AmenityRequest request);
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(UUID id);
    AmenityResponse updateAmenity(UUID id, AmenityRequest request);
    void deleteAmenity(UUID id);
}
