package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.Company;
import com.saas.backend.models.Notification;
import com.saas.backend.models.Priority;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationRequirementRepository;
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
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final AccommodationBookingRepository accommodationBookingRepository;

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
    @Transactional
    public List<NotificationResponse> getMyNotifications(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return Collections.emptyList();
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        List<Notification> rawList = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<NotificationResponse> responses = new java.util.ArrayList<>();

        for (Notification n : rawList) {
            if (isNotificationAllowedForUser(n, user)) {
                responses.add(mapToResponse(n));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public long getUnreadCount(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return 0L;
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return 0L;
        }

        List<Notification> unread = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId());
        long count = 0L;
        for (Notification n : unread) {
            if (isNotificationAllowedForUser(n, user)) {
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this notification");
        }

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
        List<Notification> toUpdate = new java.util.ArrayList<>();
        for (Notification n : unread) {
            if (isNotificationAllowedForUser(n, user)) {
                n.setReadAt(now);
                n.setStatus("READ");
                toUpdate.add(n);
            }
        }
        if (!toUpdate.isEmpty()) {
            notificationRepository.saveAll(toUpdate);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void clearAllNotifications(Authentication auth) {
        if (auth == null || auth.getName() == null) return;

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return;

        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (!list.isEmpty()) {
            notificationRepository.deleteAll(list);
        }
    }

    /**
     * Strict multi-tenant isolation validator.
     * Evaluates whether a notification legitimately belongs to the current user's tenant.
     * If a notification was previously cross-pollinated or leaked due to legacy bugs,
     * it purges it immediately to heal the database.
     */
    private boolean isNotificationAllowedForUser(Notification n, User user) {
        if (n == null || user == null) {
            return false;
        }

        // 1. Must be assigned to this user
        if (n.getUser() == null || !n.getUser().getId().equals(user.getId())) {
            return false;
        }

        boolean isPlatformAdmin = user.getRole() != null && user.getRole().getName() != null &&
                (user.getRole().getName().toUpperCase().contains("SUPER_ADMIN") ||
                 user.getRole().getName().toUpperCase().contains("PLATFORM_ADMIN"));

        Company userCompany = user.getCompany();
        if (userCompany == null || userCompany.getId() == null) {
            return isPlatformAdmin;
        }

        // 2. Direct company match check
        if (n.getCompany() != null && n.getCompany().getId() != null) {
            if (!n.getCompany().getId().equals(userCompany.getId())) {
                log.warn("Purging cross-tenant notification {} (company {} != user company {})",
                        n.getId(), n.getCompany().getId(), userCompany.getId());
                try {
                    notificationRepository.delete(n);
                } catch (Exception ex) {
                    log.error("Failed to delete leaked notification: {}", ex.getMessage());
                }
                return false;
            }
            return true;
        }

        // 3. Inspect related entity for legacy notifications where company_id is null
        if (n.getRelatedEntityType() != null && n.getRelatedEntityId() != null) {
            String entityType = n.getRelatedEntityType();
            UUID entityId = n.getRelatedEntityId();

            if ("AccommodationRequirement".equalsIgnoreCase(entityType) || "REQUIREMENT".equalsIgnoreCase(entityType)) {
                try {
                    AccommodationRequirement req = accommodationRequirementRepository.findById(entityId).orElse(null);
                    if (req != null) {
                        Company reqCompany = null;
                        if (req.getSafari() != null && req.getSafari().getClient().getCompany() != null) {
                            reqCompany = req.getSafari().getClient().getCompany();
                        } else if (req.getSafari() != null && req.getSafari().getClient() != null && req.getSafari().getClient().getCompany() != null) {
                            reqCompany = req.getSafari().getClient().getCompany();
                        }

                        if (reqCompany != null && !reqCompany.getId().equals(userCompany.getId())) {
                            log.warn("Purging legacy leaked requirement notification {} belonging to foreign company {}", n.getId(), reqCompany.getId());
                            notificationRepository.delete(n);
                            return false;
                        }

                        if (reqCompany != null && reqCompany.getId().equals(userCompany.getId())) {
                            n.setCompany(userCompany);
                            notificationRepository.save(n);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error validating requirement company for notification {}: {}", n.getId(), e.getMessage());
                }
            } else if ("AccommodationBooking".equalsIgnoreCase(entityType) || "BOOKING".equalsIgnoreCase(entityType)) {
                try {
                    AccommodationBooking booking = accommodationBookingRepository.findById(entityId).orElse(null);
                    if (booking != null) {
                        Company bookingCompany = booking.getAccommodationRequirement().getSafari().getClient().getCompany();
                        if (bookingCompany == null && booking.getAccommodationRequirement() != null && booking.getAccommodationRequirement().getSafari() != null) {
                            bookingCompany = booking.getAccommodationRequirement().getSafari().getClient().getCompany();
                        }

                        if (bookingCompany != null && !bookingCompany.getId().equals(userCompany.getId())) {
                            log.warn("Purging legacy leaked booking notification {} belonging to foreign company {}", n.getId(), bookingCompany.getId());
                            notificationRepository.delete(n);
                            return false;
                        }

                        if (bookingCompany != null && bookingCompany.getId().equals(userCompany.getId())) {
                            n.setCompany(userCompany);
                            notificationRepository.save(n);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error validating booking company for notification {}: {}", n.getId(), e.getMessage());
                }
            }
        }

        // Backfill company to user's company
        n.setCompany(userCompany);
        try {
            notificationRepository.save(n);
        } catch (Exception ignored) {}
        return true;
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
        Company company = null;
        if (user != null && user.getCompany() != null) {
            company = user.getCompany();
        }

        Notification notification = Notification.builder()
                .user(user)
                .company(company)
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
        UUID compId = n.getCompany() != null ? n.getCompany().getId() : (n.getUser() != null && n.getUser().getCompany() != null ? n.getUser().getCompany().getId() : null);
        String compName = n.getCompany() != null ? n.getCompany().getName() : (n.getUser() != null && n.getUser().getCompany() != null ? n.getUser().getCompany().getName() : null);

        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .companyId(compId)
                .companyName(compName)
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