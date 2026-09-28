package com.saas.backend.service;

import java.util.UUID;

import com.saas.backend.models.AccommodationBooking;

public interface BookingFollowUpService {

    void createInitialFollowUp(
            AccommodationBooking booking
    );

    void createReminder(
            AccommodationBooking booking
    );

    void cancelPendingFollowUps(
            UUID bookingId
    );
}