package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.OccasionRequest;
import com.saas.backend.response.SpecialOccasionResponse;

public interface OccasionService {

    SpecialOccasionResponse createGuestOccassion(UUID guestId, OccasionRequest request);

    SpecialOccasionResponse updateGuestOccasion(UUID guestId, OccasionRequest request);

    List<SpecialOccasionResponse> getGuestSpecialOcassion(UUID guestId);
    
    void deleteGuestOccassion(UUID occassionId);

}
