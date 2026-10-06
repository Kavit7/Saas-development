package com.saas.backend.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final GuestRepository guestRepository;
    private final EmailTemplateBuilder emailTemplateBuilder;

    @Value("${spring.mail.username}")
    private String systemEmail;

    @Override
    public void sendBookingRequest(AccommodationBooking booking) {
        String propertyEmail = booking.getProperty() != null ? booking.getProperty().getContactEmail() : null;
        if (propertyEmail == null || propertyEmail.isBlank()) {
            throw new IllegalArgumentException("Property email is required");
        }

        String subject = "Accommodation Reservation Request - " + booking.getReferenceNumber();
        List<Guest> guests = fetchGuestsForBooking(booking);

        String htmlBody = emailTemplateBuilder.buildBookingHtml(booking, guests, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildBookingPlainText(booking, guests, systemEmail);

        sendEmail(propertyEmail, subject, plainTextBody, htmlBody);
        log.info("Successfully dispatched multipart booking request email for booking {} to {}",
                booking.getReferenceNumber(), propertyEmail);
    }

    @Override
    public void sendBookingReminder(AccommodationBooking booking) {
        String propertyEmail = booking.getProperty() != null ? booking.getProperty().getContactEmail() : null;
        if (propertyEmail == null || propertyEmail.isBlank()) {
            throw new IllegalArgumentException("Property email is required");
        }

        String subject = "Follow-up - Accommodation Request " + booking.getReferenceNumber();
        List<Guest> guests = fetchGuestsForBooking(booking);

        String htmlBody = emailTemplateBuilder.buildReminderHtml(booking, guests, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildReminderPlainText(booking, guests, systemEmail);

        sendEmail(propertyEmail, subject, plainTextBody, htmlBody);
        log.info("Successfully dispatched multipart follow-up reminder email for booking {} to {}",
                booking.getReferenceNumber(), propertyEmail);
    }

    @Override
    public Map<String, String> generateBookingEmailPreview(AccommodationBooking booking) {
        String propertyEmail = (booking.getProperty() != null && booking.getProperty().getContactEmail() != null)
                ? booking.getProperty().getContactEmail()
                : "reservations@property.com";

        String subject = "Accommodation Reservation Request - " + booking.getReferenceNumber();
        List<Guest> guests = fetchGuestsForBooking(booking);

        String htmlBody = emailTemplateBuilder.buildBookingHtml(booking, guests, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildBookingPlainText(booking, guests, systemEmail);

        Map<String, String> preview = new LinkedHashMap<>();
        preview.put("subject", subject);
        preview.put("recipient", propertyEmail);
        preview.put("from", systemEmail);
        preview.put("referenceNumber", booking.getReferenceNumber());
        preview.put("html", htmlBody);
        preview.put("plainText", plainTextBody);

        return preview;
    }

    private void sendEmail(
            String recipient,
            String subject,
            String plainText,
            String htmlText) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            // System mailbox with executive display name
            try {
                helper.setFrom(systemEmail, "Safari Reservations Desk");
                helper.setReplyTo(systemEmail, "Safari Reservations Desk");
            } catch (Exception ex) {
                helper.setFrom(systemEmail);
                helper.setReplyTo(systemEmail);
            }

            helper.setTo(recipient);

            helper.setSubject(subject);

            // Multipart alternative: plain text fallback + rich responsive HTML layout
            helper.setText(plainText, htmlText);

            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send multipart email to {}: {}", recipient, e.getMessage());
            throw new RuntimeException("Failed to send email to " + recipient, e);
        }
    }

    private List<Guest> fetchGuestsForBooking(AccommodationBooking booking) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;

        if (client != null && client.getId() != null) {
            List<Guest> list = guestRepository.findByClientId(client.getId());
            return list != null ? list : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
