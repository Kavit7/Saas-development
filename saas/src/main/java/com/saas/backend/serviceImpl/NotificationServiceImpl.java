package com.saas.backend.serviceImpl;

import org.springframework.stereotype.Service;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.User;
import com.saas.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    @Override
    public void notifyBookingSent(
            AccommodationBooking booking) {

        User reservationManager =
                booking.getReservationManager();

        User salesPerson =
                booking.getAccommodationRequirement()
                        .getSafari()
                        .getSalesPerson();

        /*
         * Create notification for
         * Reservation Manager
         */
        System.out.println(
                "Notify Reservation Manager: "
                        + reservationManager.getEmail()
        );
        /*
         * Create notification for
         * Sales Person
         */

        System.out.println(
                "Notify Sales Person: "
                        + salesPerson.getEmail()
        );
    }

    @Override
    public void notifyBookingConfirmed(
            AccommodationBooking booking) {

        User salesPerson =
                booking.getAccommodationRequirement()
                        .getSafari()
                        .getSalesPerson();

        System.out.println(
                "Notify Sales Person: "
                        + salesPerson.getEmail()
                        + " - Accommodation confirmed."
        );
    }

    @Override
    public void notifyBookingDeclined(
            AccommodationBooking booking) {

        User salesPerson =
                booking.getAccommodationRequirement()
                        .getSafari()
                        .getSalesPerson();

        System.out.println(
                "Notify Sales Person: "
                        + salesPerson.getEmail()
                        + " - Accommodation declined."
        );
    }

    @Override
    public void notifyFollowUp(
            AccommodationBooking booking) {

        User reservationManager =
                booking.getReservationManager();

        User salesPerson =
                booking.getAccommodationRequirement()
                        .getSafari()
                        .getSalesPerson();

        System.out.println(
                "Follow-up notification to: "
                        + reservationManager.getEmail()
        );

        System.out.println(
                "Follow-up notification to: "
                        + salesPerson.getEmail()
        );
    }
}