package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.Priority;
import com.saas.backend.models.User;
import com.saas.backend.response.NotificationResponse;

/**
 * NotificationService
 * Coordinates in-app alerts, push triggers, and persistence of notifications
 * across safari bookings, requirement changes, and operational escalations.
 */
public interface NotificationService {

    /**
     * Triggered when a provisional booking request is dispatched to a property.
     */
    void notifyBookingSent(AccommodationBooking booking);

    /**
     * Triggered when a property confirms availability and accepts a booking.
     */
    void notifyBookingConfirmed(AccommodationBooking booking);

    /**
     * Triggered when a booking is declined or cancelled.
     */
    void notifyBookingDeclined(AccommodationBooking booking);

    /**
     * Triggered when incoming lodge response could not be verified automatically and requires human review.
     */
    void notifyManualReviewRequired(AccommodationBooking booking, String reason);

    /**
     * Triggered for automated or manual follow-ups on pending accommodation requests.
     */
    void notifyFollowUp(AccommodationBooking booking);

    /**
     * Creates and persists an ad-hoc notification for a specific user.
     */
    NotificationResponse createNotification(
            User user,
            String type,
            String title,
            String message,
            Priority priority,
            String entityType,
            UUID entityId
    );

    /**
     * Retrieves all notifications for the currently authenticated user.
     */
    List<NotificationResponse> getMyNotifications(Authentication auth);

    /**
     * Counts unread notifications for badge rendering.
     */
    long getUnreadCount(Authentication auth);

    /**
     * Marks a specific notification as read.
     */
    void markAsRead(UUID notificationId, Authentication auth);

    /**
     * Marks all notifications for the current user as read.
     */
    void markAllAsRead(Authentication auth);
}