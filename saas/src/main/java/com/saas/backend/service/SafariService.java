package com.saas.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.saas.backend.dto.ItineraryRequest;
import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;
import com.saas.backend.response.ItineraryDayResponse;
import com.saas.backend.response.SafariResponse;

public interface SafariService {
    SafariResponse createClientSafari(UUID clientId, SafariRequest request);
    List<ItineraryDayResponse> getItineraryDaySafari(UUID safariId);
    void updateSafariItenaryDay(UUID safariId, ItineraryUpdateRequest request);
    void deleteItineraryDay(UUID safariId, UUID dayId);
    void updateItineraryDay(UUID safariId, UUID dayId, ItineraryRequest request);
    Page<SafariResponse> getAllSafari(int page, int size, String sortBy, String search, String direction, SafariStatus status, LocalDate startDate, LocalDate endDate);
    List<ItineraryDayResponse> regenerateItineraryDays(UUID safariId);
    SafariResponse updateSafariStatus(UUID safariId, SafariStatus status);
    SafariStatus recalculateAndSaveSafariStatus(Safari safari);
}
