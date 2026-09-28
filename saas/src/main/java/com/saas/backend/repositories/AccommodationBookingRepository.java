package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.backend.models.AccommodationBooking;

public interface AccommodationBookingRepository extends JpaRepository<AccommodationBooking,UUID>{
    AccommodationBooking findByReferenceNumber(String refno);
}
