package com.staynest.api.service;

import com.staynest.api.dto.response.NotificationResponse;
import com.staynest.api.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void createNotification(UUID userId, String title, String body, NotificationType type,
                            UUID referenceId, String referenceType);

    Page<NotificationResponse> getNotificationsForUser(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);
}
