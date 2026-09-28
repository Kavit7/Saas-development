package com.saas.backend.service;

import java.util.UUID;

import com.saas.backend.dto.AccommodationBookingRequest;
import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.response.AccommodationBookingResponse;

public interface AccommodationBookingService {
    AccommodationBookingResponse createBooking(AccommodationBookingRequest request);

    AccommodationBookingResponse sendBookingRequest(UUID bookingId);

    AccommodationBookingResponse declineBooking(UUID bookingId);

    AccommodationBookingResponse confirmBooking(UUID bookingId, String confirmationNumber);

    void processEmailConfirmation(AccommodationBooking booking, IncomingMailMessage email);

    void processEmailDecline(AccommodationBooking booking, IncomingMailMessage email);
}
