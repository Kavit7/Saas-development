package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;

public interface  SafariService {
    Safari createClientSafari(UUID clientId,SafariRequest request);
    List<ItineraryDay> getItineraryDaySafari(UUID safariId);
    void updateSafariItenaryDay(UUID safarId,ItineraryUpdateRequest request);

}
