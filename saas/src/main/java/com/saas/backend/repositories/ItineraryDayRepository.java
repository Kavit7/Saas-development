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

    @org.springframework.data.jpa.repository.Query("SELECT d FROM ItineraryDay d WHERE d.id = :dayId AND d.safari.id = :safariId")
    Optional<ItineraryDay> findByIdAndSafariId(@org.springframework.data.repository.query.Param("dayId") UUID dayId, @org.springframework.data.repository.query.Param("safariId") UUID safariId);

    List<ItineraryDay> findBySafariIdOrderByDayNumberAsc(UUID safariId);
}
