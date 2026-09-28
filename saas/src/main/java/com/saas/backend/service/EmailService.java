package com.saas.backend.service;

import com.saas.backend.models.AccommodationBooking;

public interface EmailService {

    void sendBookingRequest(
            AccommodationBooking booking
    );

    void sendBookingReminder(
            AccommodationBooking booking
    );
}
