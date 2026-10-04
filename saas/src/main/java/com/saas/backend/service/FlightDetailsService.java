package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.FlightRequest;
import com.saas.backend.response.FlightDetailResponse;

public interface FlightDetailsService {
    FlightDetailResponse createClientFlightDetails(UUID clientId, FlightRequest request);
    List<FlightDetailResponse> getClientFlightDetails(UUID clientId);
}
