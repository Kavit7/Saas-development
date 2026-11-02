package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.models.*;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.BookingFollowUpRepository;
import com.saas.backend.repositories.NotificationRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.service.BookingFollowUpService;
import com.saas.backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingFollowUpServiceImpl implements BookingFollowUpService {

    private final BookingFollowUpRepository followUpRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createInitialFollowUp(AccommodationBooking booking) {
        BookingFollowUp followUp = BookingFollowUp.builder()
                .booking(booking)
                .type(FollowUpType.INITIAL_REQUEST)
                .status(FollowUpStatus.SENT)
                .scheduledAt(OffsetDateTime.now())
                .sentAt(OffsetDateTime.now())
                .message("Initial accommodation reservation request sent to property.")
                .createdBy(booking.getReservationManager())
                .build();

        followUpRepository.save(followUp);
    }

    @Override
    @Transactional
    public void createReminder(AccommodationBooking booking) {
        BookingFollowUp followUp = BookingFollowUp.builder()
                .booking(booking)
                .type(FollowUpType.REMINDER)
                .status(FollowUpStatus.SENT)
                .scheduledAt(OffsetDateTime.now())
                .sentAt(OffsetDateTime.now())
                .message("Accommodation reservation reminder sent to property.")
                .createdBy(booking.getReservationManager())
                .build();

        followUpRepository.save(followUp);
    }

    @Override
    @Transactional
    public void cancelPendingFollowUps(UUID bookingId) {
        List<BookingFollowUp> pendingFollowUps =
                followUpRepository.findByBookingIdAndStatus(
                        bookingId,
                        FollowUpStatus.SCHEDULED);

        for (BookingFollowUp followUp : pendingFollowUps) {
            followUp.setStatus(FollowUpStatus.CANCELLED);
        }

        followUpRepository.saveAll(pendingFollowUps);
    }

    /**
     * Daily scheduled cron job to scan pending provisional bookings and notify
     * Reservation Managers of unresponsive lodges so they can trigger reminders or re-allocate.
     * Default runs at 9:00 AM every morning.
     */
    @Scheduled(cron = "${app.scheduling.booking-followup-cron:0 0 9 * * ?}")
    public void scheduledDailyBookingFollowUpCheck() {
        log.info("Starting scheduled daily booking follow-up scan...");
        try {
            executeBookingFollowUpsBatch();
        } catch (Exception ex) {
            log.error("Error during scheduled booking follow-up batch: {}", ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> executeBookingFollowUpsBatch() {
        List<AccommodationBooking> pendingBookings = bookingRepository.findAllPendingProvisionalBookings();

        // Group by company
        Map<UUID, List<AccommodationBooking>> companyBookings = new LinkedHashMap<>();
        for (AccommodationBooking b : pendingBookings) {
            UUID companyId = null;
            AccommodationRequirement req = b.getAccommodationRequirement();
            if (req != null && req.getSafari() != null && req.getSafari().getClient() != null && req.getSafari().getClient().getCompany() != null) {
                companyId = req.getSafari().getClient().getCompany().getId();
            } else if (b.getReservationManager() != null && b.getReservationManager().getCompany() != null) {
                companyId = b.getReservationManager().getCompany().getId();
            }

            if (companyId != null) {
                companyBookings.computeIfAbsent(companyId, k -> new ArrayList<>()).add(b);
            }
        }

        int totalEmailsDispatched = 0;
        int totalNotificationsSaved = 0;

        for (Map.Entry<UUID, List<AccommodationBooking>> entry : companyBookings.entrySet()) {
            UUID companyId = entry.getKey();
            List<AccommodationBooking> bookings = entry.getValue();
            if (bookings.isEmpty()) continue;

            List<User> reservationManagers = userRepository.findReservationManagersByCompanyId(companyId);
            // If no RM assigned, fetch company admins/managers
            if (reservationManagers.isEmpty()) {
                reservationManagers = userRepository.findByCompanyId(companyId).stream()
                        .filter(u -> u.getRole() != null && (u.getRole().getName().toUpperCase().contains("ADMIN") || u.getRole().getName().toUpperCase().contains("MANAGER")))
                        .collect(Collectors.toList());
            }

            for (User rm : reservationManagers) {
                if (rm.getEmail() != null && !rm.getEmail().isBlank()) {
                    try {
                        emailService.sendReservationManagerBookingFollowUpAlert(rm, bookings);
                        totalEmailsDispatched++;
                    } catch (Exception e) {
                        log.error("Failed to send booking follow-up alert to RM {}: {}", rm.getEmail(), e.getMessage());
                    }
                }

                // In-app Notification
                try {
                    Notification notif = Notification.builder()
                            .user(rm)
                            .company(rm.getCompany())
                            .type("BOOKING_FOLLOW_UP_REQUIRED")
                            .title("Follow-up Digest: " + bookings.size() + " Lodge Booking(s) Awaiting Confirmation")
                            .message(String.format("You have %d accommodation booking(s) awaiting response from properties. You can trigger email reminders or re-allocate lodges directly.", bookings.size()))
                            .priority(Priority.HIGH)
                            .sentAt(OffsetDateTime.now())
                            .status("SENT")
                            .relatedEntityType("AccommodationBooking")
                            .relatedEntityId(bookings.get(0).getId())
                            .build();
                    notificationRepository.save(notif);
                    totalNotificationsSaved++;
                } catch (Exception e) {
                    log.error("Failed to save in-app follow-up notification for user {}: {}", rm.getId(), e.getMessage());
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", OffsetDateTime.now());
        summary.put("totalPendingBookings", pendingBookings.size());
        summary.put("companiesAffected", companyBookings.size());
        summary.put("emailsDispatched", totalEmailsDispatched);
        summary.put("notificationsSaved", totalNotificationsSaved);
        summary.put("status", "SUCCESS");

        log.info("Completed booking follow-up batch: pending={}, companies={}, emails={}, notifs={}",
                pendingBookings.size(), companyBookings.size(), totalEmailsDispatched, totalNotificationsSaved);
        return summary;
    }
}
