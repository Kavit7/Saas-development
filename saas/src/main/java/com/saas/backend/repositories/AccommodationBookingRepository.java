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

    @Query("SELECT COUNT(b) FROM AccommodationBooking b WHERE b.accommodationRequirement.safari.client.company.id = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT b FROM AccommodationBooking b WHERE b.accommodationRequirement.safari.client.company.id = :companyId ORDER BY b.createdAt DESC")
    List<AccommodationBooking> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT b FROM AccommodationBooking b " +
           "LEFT JOIN FETCH b.accommodationRequirement ar " +
           "LEFT JOIN FETCH ar.safari s " +
           "LEFT JOIN FETCH s.client c " +
           "LEFT JOIN FETCH c.company comp " +
           "LEFT JOIN FETCH b.property p " +
           "WHERE b.status = com.saas.backend.models.BookingStatus.PROVISIONAL " +
           "AND (s IS NULL OR s.status NOT IN (com.saas.backend.models.SafariStatus.CANCELLED, com.saas.backend.models.SafariStatus.COMPLETED)) " +
           "ORDER BY b.requestedAt ASC")
    List<AccommodationBooking> findAllPendingProvisionalBookings();

    @Query("SELECT b FROM AccommodationBooking b " +
           "LEFT JOIN FETCH b.accommodationRequirement ar " +
           "LEFT JOIN FETCH ar.safari s " +
           "LEFT JOIN FETCH s.client c " +
           "LEFT JOIN FETCH b.property p " +
           "WHERE b.status = com.saas.backend.models.BookingStatus.PROVISIONAL " +
           "AND s.client.company.id = :companyId " +
           "AND (s IS NULL OR s.status NOT IN (com.saas.backend.models.SafariStatus.CANCELLED, com.saas.backend.models.SafariStatus.COMPLETED)) " +
           "ORDER BY b.requestedAt ASC")
    List<AccommodationBooking> findPendingProvisionalByCompanyId(@Param("companyId") UUID companyId);
}
