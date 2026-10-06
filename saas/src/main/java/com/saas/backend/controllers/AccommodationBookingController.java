package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.AccommodationBookingRequest;
import com.saas.backend.dto.ConfirmBookingRequest;
import com.saas.backend.models.BookingStatus;
import com.saas.backend.response.AccommodationBookingResponse;
import com.saas.backend.service.AccommodationBookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * AccommodationBookingController
 * Handles lodge booking creation, inquiry dispatch, confirmation, and status reporting.
 */
@RestController
@RequestMapping({"/accommodation-bookings", "/api/v1/accommodation-bookings"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accommodation Bookings", description = "Lodge room reservation and confirmation management")
@RequiredArgsConstructor
public class AccommodationBookingController {

    private final AccommodationBookingService accommodationBookingService;

    /**
     * Lists accommodation bookings with pagination, search, status filtering, and multi-tenant security.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    @Operation(summary = "Get accommodation bookings with pagination and filters")
    public ResponseEntity<Page<AccommodationBookingResponse>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status
    ) {
        Page<AccommodationBookingResponse> bookings = accommodationBookingService.getBookings(
                page, size, sortBy, direction, search, status
        );
        return ResponseEntity.ok(bookings);
    }

    /**
     * Non-paginated endpoint for full list retrieval.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    @Operation(summary = "Get all accommodation bookings (non-paginated)")
    public ResponseEntity<List<AccommodationBookingResponse>> getAllBookings() {
        List<AccommodationBookingResponse> bookings = accommodationBookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Retrieves single booking by ID.
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<AccommodationBookingResponse> getBookingById(@PathVariable UUID bookingId) {
        AccommodationBookingResponse booking = accommodationBookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Retrieves bookings associated with an accommodation requirement.
     */
    @GetMapping("/by-requirement/{requirementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    @Operation(summary = "Get bookings by requirement ID")
    public ResponseEntity<List<AccommodationBookingResponse>> getBookingsByRequirement(@PathVariable UUID requirementId) {
        List<AccommodationBookingResponse> bookings = accommodationBookingService.getBookingsByRequirement(requirementId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Retrieves bookings associated with a safari expedition.
     */
    @GetMapping("/by-safari/{safariId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    @Operation(summary = "Get bookings by safari ID")
    public ResponseEntity<List<AccommodationBookingResponse>> getBookingsBySafari(@PathVariable UUID safariId) {
        List<AccommodationBookingResponse> bookings = accommodationBookingService.getBookingsBySafari(safariId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Creates a new draft booking.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Create draft accommodation booking")
    public ResponseEntity<AccommodationBookingResponse> createBooking(
            @Valid @RequestBody AccommodationBookingRequest request
    ) {
        AccommodationBookingResponse response = accommodationBookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    /**
     * Sends booking request email to the property (DRAFT -> PROVISIONAL).
     */
    @PostMapping("/{bookingId}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Send booking request to property")
    public ResponseEntity<AccommodationBookingResponse> sendBookingRequest(@PathVariable UUID bookingId) {
        AccommodationBookingResponse response = accommodationBookingService.sendBookingRequest(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a preview of the booking request email that will be sent to the property.
     */
    @GetMapping("/{bookingId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Preview accommodation booking email")
    public ResponseEntity<Map<String, String>> getBookingEmailPreview(@PathVariable UUID bookingId) {
        Map<String, String> preview = accommodationBookingService.getBookingEmailPreview(bookingId);
        return ResponseEntity.ok(preview);
    }

    /**
     * Confirms booking with property confirmation number (PROVISIONAL -> CONFIRMED).
     */
    @PostMapping("/{bookingId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Confirm booking with confirmation number")
    public ResponseEntity<AccommodationBookingResponse> confirmBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ConfirmBookingRequest request
    ) {
        AccommodationBookingResponse response = accommodationBookingService.confirmBooking(
                bookingId,
                request.getConfirmationNumber()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Declines or cancels booking (PROVISIONAL/DRAFT -> CANCELLED).
     */
    @PostMapping("/{bookingId}/decline")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Decline or cancel booking")
    public ResponseEntity<AccommodationBookingResponse> declineBooking(@PathVariable UUID bookingId) {
        AccommodationBookingResponse response = accommodationBookingService.declineBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}