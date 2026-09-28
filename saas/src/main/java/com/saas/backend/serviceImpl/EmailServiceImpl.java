
package com.saas.backend.serviceImpl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.io.ByteArrayResource;
// import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

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

    private String buildBookingEmail(
            AccommodationBooking booking) {

        return """
                Dear Property,

                We would like to request accommodation
                based on the following details.

                Booking Reference:
                %s

                Check-in:
                %s

                Check-out:
                %s

                Number of Rooms:
                %s

                Room Requirements:
                %s

                Please confirm availability and provide
                your confirmation details.

                When replying to this email, please keep
                the booking reference in the subject.

                Regards,

                Reservation Team
                """
                .formatted(
                        booking.getReferenceNumber(),
                        booking.getCheckIn(),
                        booking.getCheckOut(),
                        booking.getAccommodationRequirement()
                                .getNumberOfRooms(),
                        buildRoomRequirements(booking)
                );
    }

    private String buildReminderEmail(
            AccommodationBooking booking) {

        return """
                Dear Property,

                We are following up on our accommodation
                reservation request.

                Booking Reference:
                %s

                Check-in:
                %s

                Check-out:
                %s

                We have not yet received a response.

                Kindly provide an update regarding
                availability.

                Regards,

                Reservation Team
                """
                .formatted(
                        booking.getReferenceNumber(),
                        booking.getCheckIn(),
                        booking.getCheckOut()
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

