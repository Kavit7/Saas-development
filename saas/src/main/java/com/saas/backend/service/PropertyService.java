package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.response.AmenityResponse;
import com.saas.backend.response.PropertyResponse;
import com.saas.backend.response.TagResponse;

public interface PropertyService {
    
    PropertyResponse createProperty(PropertyRequest request);
    Page<PropertyResponse> getAllProperty(int page, int size, String sortBy, String direction, String search, VerificationStatus status);
    PropertyResponse getPropertyById(UUID propId);
    PropertyResponse UpdateProperty(UUID propId, PropertyRequest request);
    void PropertyVerification(UUID propId);

    PropertyResponse addAmenitiesToProperty(UUID propId, List<UUID> amenityIds);
    PropertyResponse removeAmenityFromProperty(UUID propId, UUID amenityId);
    List<AmenityResponse> getPropertyAmenities(UUID propId);

    PropertyResponse addTagsToProperty(UUID propId, List<UUID> tagIds);
    PropertyResponse removeTagFromProperty(UUID propId, UUID tagId);
    List<TagResponse> getPropertyTags(UUID propId);
}
