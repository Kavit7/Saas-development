package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.saas.backend.dto.GuestRequest;
import com.saas.backend.dto.GuestRequirmentRequest;
import com.saas.backend.response.GuestRequirementResponse;
import com.saas.backend.response.GuestResponse;

public interface GuestService {
    GuestResponse createGuest(UUID id, GuestRequest request);
    GuestResponse editGuest(UUID id, GuestRequest request);
    void deleteGuest(UUID id);
    GuestRequirementResponse createGuestRequirment(UUID guestId, GuestRequirmentRequest request, Authentication auth);
    List<GuestRequirementResponse> getGuestRequirement(UUID guestId);
    GuestRequirementResponse updateGuestRequirement(UUID reqId, GuestRequirmentRequest request);
    void deleteGuestRequirement(UUID reqId);
}
