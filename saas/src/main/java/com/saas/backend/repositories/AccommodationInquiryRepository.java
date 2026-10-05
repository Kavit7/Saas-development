package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.AccommodationInquiry;

@Repository
public interface AccommodationInquiryRepository extends JpaRepository<AccommodationInquiry, UUID>, JpaSpecificationExecutor<AccommodationInquiry> {

    @Query("SELECT i FROM AccommodationInquiry i WHERE i.accommodationBooking.accommodationRequirement.safari.client.company.id = :companyId ORDER BY i.createdAt DESC")
    List<AccommodationInquiry> findByCompanyId(@Param("companyId") UUID companyId);

    List<AccommodationInquiry> findByAccommodationBookingIdOrderByCreatedAtDesc(UUID bookingId);
}

