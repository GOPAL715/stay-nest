package com.staynest.api.service;

import com.staynest.api.dto.response.NotificationResponse;

import java.util.UUID;

/**
 * Sends real-time notifications to connected clients via STOMP WebSocket.
 */
public interface NotificationWebSocketService {
    /**
     * Push a notification to a specific user's personal queue.
     * Client subscribes to: /user/{userId}/queue/notifications
     */
    void pushNotificationToUser(UUID userId, NotificationResponse notification);
}
