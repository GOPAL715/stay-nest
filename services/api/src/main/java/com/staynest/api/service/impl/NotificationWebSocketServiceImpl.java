package com.staynest.api.service.impl;

import com.staynest.api.dto.response.NotificationResponse;
import com.staynest.api.service.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketServiceImpl implements NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void pushNotificationToUser(UUID userId, NotificationResponse notification) {
        String destination = "/user/" + userId.toString() + "/queue/notifications";
        try {
            messagingTemplate.convertAndSend(destination, notification);
            log.debug("Pushed notification to user [{}] via WebSocket", userId);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification to user [{}]: {}", userId, e.getMessage());
            // Don't throw — WebSocket delivery is best-effort
        }
    }
}
