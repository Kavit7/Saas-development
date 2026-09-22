package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.saas.backend.dto.GuestRequest;
import com.saas.backend.dto.GuestRequirmentRequest;
import com.saas.backend.models.Guest;
import com.saas.backend.models.GuestRequirement;
import com.saas.backend.response.GuestResponse;

public interface GuestService {
    GuestResponse createGuest(UUID id,GuestRequest request);
    Guest editGuest(UUID id,GuestRequest request);
    void deleteGuest(UUID id);
    GuestRequirement createGuestRequirment(UUID guestId,GuestRequirmentRequest request,Authentication auth);
    List<GuestRequirement> getGuestRequirement(UUID guestId);
    GuestRequirement updateGuestRequirement(UUID reqId,GuestRequirmentRequest request);
    void deleteGuestRequirement(UUID reqId);
}
