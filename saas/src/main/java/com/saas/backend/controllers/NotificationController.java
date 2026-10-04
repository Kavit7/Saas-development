package com.saas.backend.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.response.NotificationResponse;
import com.saas.backend.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * NotificationController
 * Exposes endpoints for managing staff in-app notifications and unread counters.
 */
@RestController
@RequestMapping({"/api/v1/notifications", "/notifications"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "In-app operational alerts and notification dispatch")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lists current user's notifications.
     */
    @GetMapping
    @Operation(summary = "Get user's notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(authentication);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Retrieves unread notification counter for badge indicators.
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication);
        return ResponseEntity.ok(Collections.singletonMap("unreadCount", count));
    }

    /**
     * Marks a specific notification as read.
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        notificationService.markAsRead(id, authentication);
        return ResponseEntity.ok(Collections.singletonMap("message", "Notification marked as read"));
    }

    /**
     * Marks all notifications for current user as read.
     */
    @PatchMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication);
        return ResponseEntity.ok(Collections.singletonMap("message", "All notifications marked as read"));
    }
}
