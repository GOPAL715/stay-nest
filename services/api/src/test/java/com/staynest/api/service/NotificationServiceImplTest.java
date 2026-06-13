package com.staynest.api.service;

import com.staynest.api.dto.response.NotificationResponse;
import com.staynest.api.entity.Notification;
import com.staynest.api.entity.User;
import com.staynest.api.enums.NotificationType;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.repository.NotificationRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user() {
        return User.builder()
                .email("guest@example.com")
                .passwordHash("hash")
                .firstName("Alice")
                .lastName("Guest")
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void createNotification_savesWhenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = user();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        notificationService.createNotification(
                userId, "Booking confirmed", "Your trip is booked",
                NotificationType.BOOKING_CONFIRMED, UUID.randomUUID(), "BOOKING");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_userNotFound_skipsSave() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        notificationService.createNotification(
                userId, "Title", "Body", NotificationType.EMAIL_VERIFIED, null, null);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getNotificationsForUser_returnsPage() {
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .user(user())
                .title("Hello")
                .body("World")
                .type(NotificationType.EMAIL_VERIFIED)
                .isRead(false)
                .build();

        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any()))
                .willReturn(new PageImpl<>(List.of(notification)));

        Page<NotificationResponse> result = notificationService.getNotificationsForUser(userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Hello");
    }

    @Test
    void markAsRead_sameOwner_marksRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        User owner = user();
        Notification notification = Notification.builder()
                .user(owner)
                .title("T")
                .body("B")
                .type(NotificationType.EMAIL_VERIFIED)
                .isRead(false)
                .build();

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
        given(notificationRepository.save(notification)).willReturn(notification);

        // owner.getId() is null in unit test — pass null as userId
        notificationService.markAsRead(notificationId, null);

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markAsRead_wrongOwner_throwsAccessDeniedException() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .user(user())
                .title("T")
                .body("B")
                .type(NotificationType.EMAIL_VERIFIED)
                .isRead(false)
                .build();

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class,
                () -> notificationService.markAsRead(notificationId, UUID.randomUUID()));
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFoundException() {
        UUID notificationId = UUID.randomUUID();
        given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(notificationId, UUID.randomUUID()));
    }

    @Test
    void markAllAsRead_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        notificationService.markAllAsRead(userId);
        verify(notificationRepository).markAllAsReadForUser(userId);
    }

    @Test
    void getUnreadCount_returnsCount() {
        UUID userId = UUID.randomUUID();
        given(notificationRepository.countByUserIdAndIsReadFalse(userId)).willReturn(3L);
        assertThat(notificationService.getUnreadCount(userId)).isEqualTo(3L);
    }
}
