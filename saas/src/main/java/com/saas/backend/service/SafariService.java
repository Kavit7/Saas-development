package com.saas.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.saas.backend.dto.ItineraryRequest;
import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;

public interface  SafariService {
    Safari createClientSafari(UUID clientId,SafariRequest request);
    List<ItineraryDay> getItineraryDaySafari(UUID safariId);
    void updateSafariItenaryDay(UUID safarId,ItineraryUpdateRequest request);
    void deleteItineraryDay(UUID safariId, UUID dayId);
    void updateItineraryDay(UUID safariId,UUID dayId,ItineraryRequest request);
    Page<Safari> getAllSafari(int page,int size,String sortBy,String search,String direction,SafariStatus status, LocalDate startDate,LocalDate endDate);
}
