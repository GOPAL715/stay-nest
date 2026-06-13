package com.staynest.api.controller;

import com.staynest.api.dto.response.NotificationResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket controller for real-time notification delivery.
 *
 * Clients connect via SockJS at /ws, then subscribe to:
 *   /user/{userId}/queue/notifications  — personal notification stream
 *
 * Clients can also send a message to /app/notifications/fetch
 * to get their latest unread notifications on (re)connect.
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "WebSocket Notifications", description = "STOMP real-time notification endpoints")
public class NotificationWebSocketController {

    private final NotificationService notificationService;

    /**
     * Client sends to /app/notifications/fetch
     * Response sent to /user/{principal}/queue/notifications
     */
    @MessageMapping("/notifications/fetch")
    @SendToUser("/queue/notifications")
    @Operation(summary = "Fetch latest notifications on WebSocket connect (STOMP)")
    public Page<NotificationResponse> fetchNotifications(
            @AuthenticationPrincipal User currentUser) {
        return notificationService.getNotificationsForUser(
                currentUser.getId(), PageRequest.of(0, 20));
    }
}
