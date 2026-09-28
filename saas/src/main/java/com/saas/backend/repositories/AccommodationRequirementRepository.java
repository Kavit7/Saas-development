package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.ItineraryDay;

@Repository 
public interface AccommodationRequirementRepository extends JpaRepository<AccommodationRequirement,UUID> {
    Optional<AccommodationRequirement> findByItineraryDayId(UUID itineraryId);

    List<AccommodationRequirement> findBySafariId(UUID safariId);
    boolean existsByItineraryDay(ItineraryDay itineraryDay);
}
