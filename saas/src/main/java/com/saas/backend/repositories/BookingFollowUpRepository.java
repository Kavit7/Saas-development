package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.saas.backend.models.BookingFollowUp;
import com.saas.backend.models.FollowUpStatus;


public interface BookingFollowUpRepository
        extends JpaRepository<BookingFollowUp, UUID> {

  List<BookingFollowUp> findByBookingIdAndStatus(UUID bookingId, FollowUpStatus status);
}

