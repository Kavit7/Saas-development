package com.saas.backend.service;

import java.util.Map;

import com.saas.backend.models.AccommodationBooking;

public interface EmailService {

    void sendBookingRequest(
            AccommodationBooking booking
    );

    void sendBookingReminder(
            AccommodationBooking booking
    );

    Map<String, String> generateBookingEmailPreview(
            AccommodationBooking booking
    );
}
