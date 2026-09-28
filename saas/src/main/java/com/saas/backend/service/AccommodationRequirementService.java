package com.saas.backend.service;

import java.util.UUID;

import com.saas.backend.dto.RequirementRequest;

import com.saas.backend.response.AccommodationRequirementResponse;

public interface AccommodationRequirementService {
    
AccommodationRequirementResponse createAccommodationRequirement(UUID itineraryId,RequirementRequest request);

AccommodationRequirementResponse updateAccommodationRequirement(UUID requirementId, RequirementRequest request);

void deleteAccommodationRequirement(UUID requirementId);

AccommodationRequirementResponse getAccommodationRequirementById(UUID requirementId);


}
