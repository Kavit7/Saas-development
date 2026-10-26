package com.saas.backend.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.UnpaidAccommodationAlertDto;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.Client;
import com.saas.backend.models.Guest;
import com.saas.backend.models.GuestRequirement;
import com.saas.backend.models.Safari;
import com.saas.backend.models.User;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.GuestRequirementRepository;
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
    private final GuestRequirementRepository guestRequirementRepository;
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
        Map<UUID, List<GuestRequirement>> guestRequirements = fetchGuestRequirements(guests);

        String htmlBody = emailTemplateBuilder.buildBookingHtml(booking, guests, guestRequirements, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildBookingPlainText(booking, guests, guestRequirements, systemEmail);

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
        Map<UUID, List<GuestRequirement>> guestRequirements = fetchGuestRequirements(guests);

        String htmlBody = emailTemplateBuilder.buildReminderHtml(booking, guests, guestRequirements, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildReminderPlainText(booking, guests, guestRequirements, systemEmail);

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
        Map<UUID, List<GuestRequirement>> guestRequirements = fetchGuestRequirements(guests);

        String htmlBody = emailTemplateBuilder.buildBookingHtml(booking, guests, guestRequirements, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildBookingPlainText(booking, guests, guestRequirements, systemEmail);

        Map<String, String> preview = new LinkedHashMap<>();
        preview.put("subject", subject);
        preview.put("recipient", propertyEmail);
        preview.put("from", systemEmail);
        preview.put("referenceNumber", booking.getReferenceNumber());
        preview.put("html", htmlBody);
        preview.put("plainText", plainTextBody);

        return preview;
    }

    @Override
    public void sendSalesPaymentDeadlineAlert(User salesPerson, List<UnpaidAccommodationAlertDto> alerts) {
        if (salesPerson == null || salesPerson.getEmail() == null || salesPerson.getEmail().isBlank()) {
            log.warn("Cannot send payment deadline alert: Sales person or email is missing");
            return;
        }
        if (alerts == null || alerts.isEmpty()) {
            return;
        }

        boolean hasOverdue = alerts.stream().anyMatch(a -> "OVERDUE".equalsIgnoreCase(a.getUrgency()));
        boolean hasImminent = alerts.stream().anyMatch(a -> "DUE_IN_1_DAY".equalsIgnoreCase(a.getUrgency()) || "UPCOMING_SAFARI_UNPAID".equalsIgnoreCase(a.getUrgency()));

        String subjectPrefix = hasOverdue ? "⏰ [URGENT] Payment Overdue: " : (hasImminent ? "⏰ [URGENT] 1 Day Left: " : "⏰ [REMINDER] Payment Deadline: ");
        String subject = subjectPrefix + alerts.size() + " Accommodation(s) Expiring Soon";

        String htmlBody = emailTemplateBuilder.buildSalesPaymentAlertHtml(salesPerson, alerts, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildSalesPaymentAlertPlainText(salesPerson, alerts, systemEmail);

        sendEmail(salesPerson.getEmail(), subject, plainTextBody, htmlBody);
        log.info("Successfully dispatched payment deadline alert email to sales consultant {} ({}) for {} accommodations",
                salesPerson.getFirstName(), salesPerson.getEmail(), alerts.size());
    }

    @Override
    public void sendReservationManagerPaymentSummary(User rm, Map<User, List<UnpaidAccommodationAlertDto>> groupedAlerts) {
        if (rm == null || rm.getEmail() == null || rm.getEmail().isBlank()) {
            log.warn("Cannot send RM payment summary: RM or email is missing");
            return;
        }
        if (groupedAlerts == null || groupedAlerts.isEmpty()) {
            return;
        }

        int totalCount = groupedAlerts.values().stream().mapToInt(l -> l != null ? l.size() : 0).sum();
        if (totalCount == 0) return;

        String subject = "📊 Daily Summary: " + totalCount + " accommodations expiring (" + groupedAlerts.size() + " sales consultants)";
        String htmlBody = emailTemplateBuilder.buildReservationManagerPaymentSummaryHtml(rm, groupedAlerts, systemEmail);
        String plainTextBody = emailTemplateBuilder.buildReservationManagerPaymentSummaryPlainText(rm, groupedAlerts, systemEmail);

        sendEmail(rm.getEmail(), subject, plainTextBody, htmlBody);
        log.info("Successfully dispatched daily payment summary email to RM {} ({}) covering {} accommodations",
                rm.getFirstName(), rm.getEmail(), totalCount);
    }

    @Override
    public void sendLodgeReplyAlert(User recipient, AccommodationBooking booking, com.saas.backend.dto.IncomingMailMessage reply, com.saas.backend.dto.EmailAnalysisResult analysis) {
        if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            log.warn("Cannot send lodge reply alert: recipient or recipient email is missing");
            return;
        }

        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? booking.getProperty().getName()
                : "Lodge";
        String statusPrefix = analysis != null && analysis.getResponseType() != null
                ? "[" + analysis.getResponseType().name() + "]"
                : "[REPLY]";

        String subject = statusPrefix + " Lodge Reply: " + propName + " - " + booking.getReferenceNumber();
        String htmlBody = emailTemplateBuilder.buildLodgeReplyAlertHtml(recipient, booking, reply, analysis, systemEmail);
        String plainText = emailTemplateBuilder.buildLodgeReplyAlertPlainText(recipient, booking, reply, analysis, systemEmail);

        try {
            sendEmail(recipient.getEmail(), subject, plainText, htmlBody);
            log.info("Successfully dispatched lodge reply alert email to {} ({}) for booking {}",
                    recipient.getFirstName(), recipient.getEmail(), booking.getReferenceNumber());
        } catch (Exception ex) {
            log.warn("Failed to dispatch lodge reply alert to {}: {}", recipient.getEmail(), ex.getMessage());
        }
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

    private Map<UUID, List<GuestRequirement>> fetchGuestRequirements(List<Guest> guests) {
        if (guests == null || guests.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UUID> guestIds = guests.stream()
                .map(Guest::getId)
                .filter(Objects::nonNull)
                .toList();
        if (guestIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<GuestRequirement> allReqs = guestRequirementRepository.findByGuestIdIn(guestIds);
            if (allReqs == null || allReqs.isEmpty()) {
                return Collections.emptyMap();
            }
            return allReqs.stream()
                    .filter(r -> r.getGuest() != null && r.getGuest().getId() != null)
                    .collect(Collectors.groupingBy(r -> r.getGuest().getId()));
        } catch (Exception e) {
            log.warn("Could not batch load guest requirements: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
