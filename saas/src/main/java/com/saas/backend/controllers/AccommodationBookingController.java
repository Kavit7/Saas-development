package com.saas.backend.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.AccommodationBookingRequest;

import com.saas.backend.response.AccommodationBookingResponse;
import com.saas.backend.service.AccommodationBookingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accommodation-bookings")
@SecurityRequirement (name="bearerAuth")
@RequiredArgsConstructor
public class AccommodationBookingController {

    private final AccommodationBookingService accommodationBookingService;

    /**
     * Create a new draft booking
     */
     @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    @PostMapping
    public ResponseEntity<AccommodationBookingResponse> createBooking(
            @Valid @RequestBody AccommodationBookingRequest request) {

        AccommodationBookingResponse response =
                accommodationBookingService.createBooking(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Send booking request email to the property (DRAFT -> PROVISIONAL)
     */
     @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    @PostMapping("/{bookingId}/send")
    public ResponseEntity<AccommodationBookingResponse> sendBookingRequest(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                accommodationBookingService.sendBookingRequest(bookingId));
    }

    /**
     * Manually mark booking as declined (PROVISIONAL -> CANCELLED)
     */
     @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    @PostMapping("/{bookingId}/decline")
    public ResponseEntity<AccommodationBookingResponse> declineBooking(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                accommodationBookingService.declineBooking(bookingId));
    }

    /**
     * Manually confirm booking with confirmation number (PROVISIONAL -> CONFIRMED)
     */
    // @PostMapping("/{bookingId}/confirm")
    // public ResponseEntity<AccommodationBookingResponse> confirmBooking(
    //         @PathVariable UUID bookingId,
    //         @Valid @RequestBody ConfirmBookingRequest request) {

    //     return ResponseEntity.ok(
    //             accommodationBookingService.confirmBooking(
    //                     bookingId,
    //                     request.getConfirmationNumber()));
    // }
}