package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.Notification;
import com.saas.backend.models.Priority;
import com.saas.backend.models.User;
import com.saas.backend.repositories.NotificationRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.NotificationResponse;
import com.saas.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationServiceImpl
 * Concrete implementation of notification dispatching and persistence.
 * Alerts relevant staff (Reservation Managers, Sales Consultants, Operations)
 * whenever booking states transition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void notifyBookingSent(AccommodationBooking booking) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Lodge";
        String refNo = booking.getReferenceNumber() != null ? booking.getReferenceNumber() : "REF";

        // 1. Notify the Reservation Manager responsible for this booking
        User rm = booking.getReservationManager();
        if (rm != null) {
            saveNotification(
                    rm,
                    "BOOKING_SENT",
                    "Booking Request Dispatched",
                    String.format("Booking %s for property '%s' has been sent to partner lodge awaiting provisional acceptance.", refNo, propName),
                    Priority.MEDIUM,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        // 2. Notify the Sales Person managing the parent safari
        if (booking.getAccommodationRequirement() != null
                && booking.getAccommodationRequirement().getSafari() != null
                && booking.getAccommodationRequirement().getSafari().getSalesPerson() != null) {

            User salesPerson = booking.getAccommodationRequirement().getSafari().getSalesPerson();
            saveNotification(
                    salesPerson,
                    "BOOKING_SENT",
                    "Lodge Reservation Initiated",
                    String.format("Lodge request (%s) at '%s' has been dispatched for Safari '%s'.",
                            refNo,
                            propName,
                            booking.getAccommodationRequirement().getSafari().getReferenceNumber()),
                    Priority.LOW,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        log.info("Dispatched in-app notifications for booking sent: {}", refNo);
    }

    @Override
    @Transactional
    public void notifyBookingConfirmed(AccommodationBooking booking) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Lodge";
        String confNo = booking.getConfirmationNumber() != null ? booking.getConfirmationNumber() : "CONFIRMED";
        String refNo = booking.getReferenceNumber() != null ? booking.getReferenceNumber() : "REF";

        // 1. Notify Sales Person
        if (booking.getAccommodationRequirement() != null
                && booking.getAccommodationRequirement().getSafari() != null
                && booking.getAccommodationRequirement().getSafari().getSalesPerson() != null) {

            User salesPerson = booking.getAccommodationRequirement().getSafari().getSalesPerson();
            saveNotification(
                    salesPerson,
                    "BOOKING_CONFIRMED",
                    "Accommodation Confirmed!",
                    String.format("Great news! '%s' has confirmed booking %s (Confirmation Code: %s) for Safari %s.",
                            propName,
                            refNo,
                            confNo,
                            booking.getAccommodationRequirement().getSafari().getReferenceNumber()),
                    Priority.HIGH,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        // 2. Notify Reservation Manager
        User rm = booking.getReservationManager();
        if (rm != null) {
            saveNotification(
                    rm,
                    "BOOKING_CONFIRMED",
                    "Booking Confirmed by Property",
                    String.format("Property '%s' verified booking %s. Confirmation number recorded: %s.",
                            propName, refNo, confNo),
                    Priority.HIGH,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        log.info("Dispatched in-app notifications for booking confirmed: {} ({})", refNo, confNo);
    }

    @Override
    @Transactional
    public void notifyBookingDeclined(AccommodationBooking booking) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Lodge";
        String refNo = booking.getReferenceNumber() != null ? booking.getReferenceNumber() : "REF";

        // 1. Alert Sales Person that accommodation was not secured
        if (booking.getAccommodationRequirement() != null
                && booking.getAccommodationRequirement().getSafari() != null
                && booking.getAccommodationRequirement().getSafari().getSalesPerson() != null) {

            User salesPerson = booking.getAccommodationRequirement().getSafari().getSalesPerson();
            saveNotification(
                    salesPerson,
                    "BOOKING_DECLINED",
                    "Lodge Booking Declined",
                    String.format("Attention: Booking %s at '%s' was declined or cancelled. An alternative allocation is required.",
                            refNo, propName),
                    Priority.URGENT,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        // 2. Alert Reservation Manager to seek alternatives
        User rm = booking.getReservationManager();
        if (rm != null) {
            saveNotification(
                    rm,
                    "BOOKING_DECLINED",
                    "Action Required: Allocation Declined",
                    String.format("Booking %s at '%s' was declined. Please review requirements and select a secondary property.",
                            refNo, propName),
                    Priority.URGENT,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        log.warn("Dispatched in-app alert for declined booking: {}", refNo);
    }

    @Override
    @Transactional
    public void notifyManualReviewRequired(AccommodationBooking booking, String reason) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Lodge";
        String refNo = booking.getReferenceNumber() != null ? booking.getReferenceNumber() : "REF";

        User rm = booking.getReservationManager();
        if (rm != null) {
            saveNotification(
                    rm,
                    "MANUAL_REVIEW_REQUIRED",
                    "Action Needed: Verify Lodge Response",
                    String.format("Inbound email from '%s' for booking %s requires manual verification (%s). Please review the email and confirm or decline manually.",
                            propName, refNo, reason != null ? reason : "Automated detection ambiguous"),
                    Priority.HIGH,
                    "AccommodationBooking",
                    booking.getId()
            );
        }
        log.info("Dispatched in-app manual review alert for booking: {}", refNo);
    }

    @Override
    @Transactional
    public void notifyFollowUp(AccommodationBooking booking) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Lodge";
        String refNo = booking.getReferenceNumber() != null ? booking.getReferenceNumber() : "REF";

        User rm = booking.getReservationManager();
        if (rm != null) {
            saveNotification(
                    rm,
                    "BOOKING_FOLLOW_UP",
                    "Follow-up Reminder: Pending Lodge Response",
                    String.format("Follow-up reminder sent for booking %s at '%s'. Still awaiting property provisional response.",
                            refNo, propName),
                    Priority.MEDIUM,
                    "AccommodationBooking",
                    booking.getId()
            );
        }
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(
            User user,
            String type,
            String title,
            String message,
            Priority priority,
            String entityType,
            UUID entityId
    ) {
        Notification notification = saveNotification(user, type, title, message, priority, entityType, entityId);
        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return Collections.emptyList();
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return 0L;
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return 0L;
        }

        return notificationRepository.countByUserIdAndReadAtIsNull(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, Authentication auth) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        notification.setReadAt(OffsetDateTime.now());
        notification.setStatus("READ");
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Authentication auth) {
        if (auth == null || auth.getName() == null) return;

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return;

        List<Notification> unread = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId());
        OffsetDateTime now = OffsetDateTime.now();
        for (Notification n : unread) {
            n.setReadAt(now);
            n.setStatus("READ");
        }
        notificationRepository.saveAll(unread);
    }

    /**
     * Helper to persist a notification record.
     */
    private Notification saveNotification(
            User user,
            String type,
            String title,
            String message,
            Priority priority,
            String entityType,
            UUID entityId
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : Priority.MEDIUM)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .sentAt(OffsetDateTime.now())
                .status("SENT")
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Maps internal entity to response DTO.
     */
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .priority(n.getPriority())
                .relatedEntityType(n.getRelatedEntityType())
                .relatedEntityId(n.getRelatedEntityId())
                .scheduledAt(n.getScheduledAt())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}