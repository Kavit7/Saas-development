package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.FlightRequest;
import com.saas.backend.models.FlightDetail;

public interface FlightDetailsService {
    FlightDetail createClientFlightDetails(UUID clientId,FlightRequest request);
     List<FlightDetail> getClientFlightDetails(UUID clientId);
}
