package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.AccommodationBookingRequest;
import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.BookingStatus;
import com.saas.backend.response.AccommodationBookingResponse;
import org.springframework.data.domain.Page;

/**
 * AccommodationBookingService
 * Core business contract for lodge room reservations, provisional requests,
 * confirmation codes, and status transitions.
 */
public interface AccommodationBookingService {

    /**
     * Lists accommodation bookings with pagination, multi-tenancy validation, and filtering.
     */
    Page<AccommodationBookingResponse> getBookings(
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            BookingStatus status
    );

    /**
     * Lists all accommodation bookings in the system ordered by newest first.
     */
    List<AccommodationBookingResponse> getAllBookings();

    /**
     * Retrieves single booking by unique ID.
     */
    AccommodationBookingResponse getBookingById(UUID bookingId);

    /**
     * Retrieves all bookings created for a specific accommodation requirement.
     */
    List<AccommodationBookingResponse> getBookingsByRequirement(UUID requirementId);

    /**
     * Retrieves all bookings attached to a specific safari expedition.
     */
    List<AccommodationBookingResponse> getBookingsBySafari(UUID safariId);

    /**
     * Creates a new draft accommodation booking.
     */
    AccommodationBookingResponse createBooking(AccommodationBookingRequest request);

    /**
     * Dispatches the booking request to the property's contact email.
     */
    AccommodationBookingResponse sendBookingRequest(UUID bookingId);

    /**
     * Declines/cancels a booking.
     */
    AccommodationBookingResponse declineBooking(UUID bookingId);

    /**
     * Confirms a booking with an official property confirmation code.
     */
    AccommodationBookingResponse confirmBooking(UUID bookingId, String confirmationNumber);

    /**
     * Handles automated inbound email confirmations.
     */
    void processEmailConfirmation(AccommodationBooking booking, IncomingMailMessage email);

    /**
     * Handles automated inbound email declines.
     */
    void processEmailDecline(AccommodationBooking booking, IncomingMailMessage email);

    /**
     * Generates a preview of the email (subject, html, plain text, recipient) that will be sent to the lodge.
     */
    java.util.Map<String, String> getBookingEmailPreview(java.util.UUID bookingId);
}

