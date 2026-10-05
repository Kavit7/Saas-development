package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.BookingStatus;

/**
 * AccommodationBookingRepository
 * Data access operations for lodge/hotel accommodation bookings.
 */
@Repository
public interface AccommodationBookingRepository extends JpaRepository<AccommodationBooking, UUID>, JpaSpecificationExecutor<AccommodationBooking> {

    AccommodationBooking findByReferenceNumber(String refno);

    List<AccommodationBooking> findByAccommodationRequirementId(UUID requirementId);

    List<AccommodationBooking> findByPropertyId(UUID propertyId);

    List<AccommodationBooking> findAllByOrderByCreatedAtDesc();

    long countByStatus(BookingStatus status);

    @Query("SELECT b FROM AccommodationBooking b WHERE b.accommodationRequirement.safari.id = :safariId ORDER BY b.checkIn ASC")
    List<AccommodationBooking> findBySafariId(@Param("safariId") UUID safariId);
}
