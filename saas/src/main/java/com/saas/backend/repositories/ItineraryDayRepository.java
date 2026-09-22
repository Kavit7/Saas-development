package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.ItineraryDay;



@Repository 
public interface ItineraryDayRepository extends JpaRepository<ItineraryDay,UUID> {
    List<ItineraryDay> findBySafariId(UUID safarId);
   Optional< ItineraryDay>findByIdAndSafariId(UUID itineraryId, UUID safariId);
}
