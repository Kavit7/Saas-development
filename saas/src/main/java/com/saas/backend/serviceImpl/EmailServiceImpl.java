
package com.saas.backend.serviceImpl;

import java.nio.charset.StandardCharsets;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.Client;
import com.saas.backend.models.Guest;
import com.saas.backend.models.Safari;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final GuestRepository guestRepository;

    @Value("${spring.mail.username}")
    private String systemEmail;

    @Override
    public void sendBookingRequest(
            AccommodationBooking booking) {

        String propertyEmail =
                booking.getProperty()
                        .getContactEmail();

        if (propertyEmail == null
                || propertyEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Property email is required"
            );
        }

        String subject =
                "Accommodation Reservation Request - "
                        + booking.getReferenceNumber();

        String body =
                buildBookingEmail(booking);

        sendEmail(
                propertyEmail,
                subject,
                body
        );
    }

    @Override
    public void sendBookingReminder(
            AccommodationBooking booking) {

        String propertyEmail =
                booking.getProperty()
                        .getContactEmail();

        if (propertyEmail == null
                || propertyEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Property email is required"
            );
        }

        String subject =
                "Follow-up - Accommodation Request "
                        + booking.getReferenceNumber();

        String body =
                buildReminderEmail(booking);

        sendEmail(
                propertyEmail,
                subject,
                body
        );
    }

    private void sendEmail(
            String recipient,
            String subject,
            String body) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            StandardCharsets.UTF_8.name()
                    );

            /*
             * System mailbox
             */
            helper.setFrom(systemEmail);

            /*
             * Property email
             */
            helper.setTo(recipient);

            /*
             * IMPORTANT:
             * When property clicks Reply,
             * response comes back to system mailbox.
             */
            helper.setReplyTo(systemEmail);

            helper.setSubject(subject);

            helper.setText(
                    body,
                    false
            );

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send email to "
                            + recipient,
                    e
            );
        }
    }

    private String buildBookingEmail(AccommodationBooking booking) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;

        String safariRef = safari != null ? safari.getReferenceNumber() : "N/A";
        String itineraryDayText = (req != null && req.getItineraryDay() != null)
                ? ("Day " + req.getItineraryDay().getDayNumber() + " - " + (req.getItineraryDay().getDestination() != null ? req.getItineraryDay().getDestination() : "Route Scheduled"))
                : "Scheduled Route";

        String leadClientName = client != null
                ? (client.getFirstName() + " " + client.getLastName())
                : "Lead Client";

        String totalGuests = (safari != null && safari.getNumberOfPassengers() != null)
                ? String.valueOf(safari.getNumberOfPassengers())
                : "1";

        String guestsListText = buildGuestList(client);
        String specialRequestsText = (req != null && req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank())
                ? req.getSpecialRequests().trim()
                : "None specified";

        String roomPreferencesText = (req != null && req.getRoomPreferences() != null && !req.getRoomPreferences().isBlank())
                ? req.getRoomPreferences().trim()
                : "Standard configuration";

        String notesText = (booking.getNotes() != null && !booking.getNotes().isBlank())
                ? booking.getNotes().trim()
                : "None";

        String numRooms = (req != null && req.getNumberOfRooms() != null)
                ? String.valueOf(req.getNumberOfRooms())
                : "1";

        return """
                Dear Reservations Team,

                Please accept our accommodation reservation request based on the following itinerary details:

                ====================================================
                RESERVATION OVERVIEW
                ====================================================
                Booking Reference:   %s
                Safari Reference:    %s
                Scheduled Day:       %s
                Check-in Date:       %s
                Check-out Date:      %s
                Total Rooms:         %s
                Total Guests:        %s

                ====================================================
                ROOM REQUIREMENTS
                ====================================================
                %s
                Room Preferences:
                %s

                ====================================================
                GUEST INFORMATION
                ====================================================
                Lead Guest:          %s
                All Guests:
                %s

                ====================================================
                SPECIAL REQUESTS & REQUIREMENTS
                ====================================================
                %s

                Additional Notes:
                %s

                ====================================================
                ACTION REQUIRED
                ====================================================
                Please reply to this email confirming room availability and provide your reservation/confirmation voucher number.
                Kindly keep the reference '%s' in the subject line to ensure automatic processing.

                Kind regards,
                Reservations Desk
                """
                .formatted(
                        booking.getReferenceNumber(),
                        safariRef,
                        itineraryDayText,
                        booking.getCheckIn(),
                        booking.getCheckOut(),
                        numRooms,
                        totalGuests,
                        buildRoomRequirements(booking),
                        roomPreferencesText,
                        leadClientName,
                        guestsListText,
                        specialRequestsText,
                        notesText,
                        booking.getReferenceNumber()
                );
    }

    private String buildGuestList(Client client) {
        if (client == null) return "- Details to follow";
        List<Guest> guests = guestRepository.findByClientId(client.getId());
        if (guests == null || guests.isEmpty()) {
            return "- " + client.getFirstName() + " " + client.getLastName() + " (Lead Guest)";
        }
        StringBuilder sb = new StringBuilder();
        for (Guest g : guests) {
            sb.append("- ").append(g.getFirstName()).append(" ").append(g.getLastName());
            if (g.getGender() != null) {
                sb.append(" (").append(g.getGender()).append(")");
            }
            if (g.getNationality() != null && !g.getNationality().isBlank()) {
                sb.append(" [").append(g.getNationality()).append("]");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String buildReminderEmail(AccommodationBooking booking) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        String safariRef = safari != null ? safari.getReferenceNumber() : "N/A";

        return """
                Dear Reservations Team,

                We are following up on our accommodation reservation request.

                Booking Reference:   %s
                Safari Reference:    %s
                Check-in Date:       %s
                Check-out Date:      %s

                Room Requirements:
                %s

                Special Requests:
                %s

                We have not yet received a response regarding room availability.
                Kindly confirm at your earliest convenience, keeping '%s' in the subject line.

                Kind regards,
                Reservations Desk
                """
                .formatted(
                        booking.getReferenceNumber(),
                        safariRef,
                        booking.getCheckIn(),
                        booking.getCheckOut(),
                        buildRoomRequirements(booking),
                        (req != null && req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank())
                                ? req.getSpecialRequests().trim() : "None",
                        booking.getReferenceNumber()
                );
    }

    private String buildRoomRequirements(
            AccommodationBooking booking) {

        if (booking.getAccommodationRequirement()
                .getRooms() == null
                || booking.getAccommodationRequirement()
                        .getRooms()
                        .isEmpty()) {

            return "No specific room requirements";
        }

        StringBuilder rooms =
                new StringBuilder();

        booking.getAccommodationRequirement()
                .getRooms()
                .forEach(room -> {

                    rooms.append("- ")
                            .append(
                                    room.getRoomType()
                                            .getName()
                            )
                            .append(": ")
                            .append(
                                    room.getQuantity()
                            )
                            .append("\n");
                });

        return rooms.toString();
    }
}

