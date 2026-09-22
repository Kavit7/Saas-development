package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.FlightDetail;
import com.saas.backend.models.FlightType;

@Repository 
public interface FlightDetailsRepository extends JpaRepository<FlightDetail,UUID> {

    boolean existsByClientIdAndFlightType(UUID clientId,FlightType flightType);
    List<FlightDetail> findAllByClientId(UUID clientId);
} 