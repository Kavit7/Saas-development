package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Notification;

/**
 * NotificationRepository
 * Manages database persistence and queries for in-app user notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Finds notifications for a specific user ordered by latest creation date.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Finds unread notifications for a user (readAt is null).
     */
    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);

    /**
     * Counts unread notifications for the user's header badge.
     */
    long countByUserIdAndReadAtIsNull(UUID userId);
}
