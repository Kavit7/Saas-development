package com.saas.backend.service;


import java.util.UUID;

import org.springframework.data.domain.Page;

import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.Property;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.response.PropertyResponse;

public interface PropertyService {
    

    PropertyResponse createProperty(PropertyRequest request);
    Page<Property> getAllProperty(int page ,int size,String sortBy,String direction, String search, VerificationStatus status);
    Property getPropertyById(UUID PropId);
    Property UpdateProperty(UUID propId,PropertyRequest request);
    void PropertyVerification(UUID propId);

}
