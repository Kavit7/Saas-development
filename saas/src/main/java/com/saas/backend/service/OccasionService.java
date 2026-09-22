package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.OccasionRequest;
import com.saas.backend.models.SpecialOccasion;

public interface OccasionService {

    SpecialOccasion createGuestOccassion(UUID guestId,OccasionRequest request);

    SpecialOccasion updateGuestOccasion(UUID guestId,OccasionRequest request);

    List<SpecialOccasion> getGuestSpecialOcassion(UUID guestId);
    void deleteGuestOccassion(UUID occassionId);
    

}
