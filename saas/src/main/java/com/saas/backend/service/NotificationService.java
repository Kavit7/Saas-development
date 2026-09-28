package com.saas.backend.service;

import com.saas.backend.models.AccommodationBooking;

public interface NotificationService {

    void notifyBookingSent(
            AccommodationBooking booking
    );

    void notifyBookingConfirmed(
            AccommodationBooking booking
    );

    void notifyBookingDeclined(
            AccommodationBooking booking
    );

    void notifyFollowUp(
            AccommodationBooking booking
    );
}