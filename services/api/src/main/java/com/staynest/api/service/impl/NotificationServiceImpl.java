package com.staynest.api.service.impl;

import com.staynest.api.dto.response.NotificationResponse;
import com.staynest.api.entity.Notification;
import com.staynest.api.entity.User;
import com.staynest.api.enums.NotificationType;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.repository.NotificationRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(UUID userId, String title, String body, NotificationType type,
                                   UUID referenceId, String referenceType) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification — user not found: {}", userId);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!Objects.equals(notification.getUser().getId(), userId)) {
            throw new AccessDeniedException("You do not own this notification");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .isRead(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
