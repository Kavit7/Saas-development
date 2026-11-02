package com.saas.backend.service;

import java.util.List;
import java.util.Map;

import com.saas.backend.dto.UnpaidAccommodationAlertDto;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.User;

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

    void sendSalesPaymentDeadlineAlert(
            User salesPerson,
            List<UnpaidAccommodationAlertDto> alerts
    );

    void sendReservationManagerPaymentSummary(
            User rm,
            Map<User, List<UnpaidAccommodationAlertDto>> groupedAlerts
    );

    void sendLodgeReplyAlert(
            User recipient,
            AccommodationBooking booking,
            com.saas.backend.dto.IncomingMailMessage reply,
            com.saas.backend.dto.EmailAnalysisResult analysis
    );

    void sendLodgeAmendmentNotice(
            AccommodationBooking booking,
            java.time.LocalDate oldCheckIn,
            java.time.LocalDate oldCheckOut,
            java.time.LocalDate newCheckIn,
            java.time.LocalDate newCheckOut,
            String reason
    );

    void sendReservationManagerBookingFollowUpAlert(
            User rm,
            List<AccommodationBooking> pendingBookings
    );
}
