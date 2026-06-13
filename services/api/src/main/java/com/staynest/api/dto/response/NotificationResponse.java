package com.staynest.api.dto.response;

import com.staynest.api.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "User notification")
public class NotificationResponse {

    private UUID id;
    private String title;
    private String body;
    private NotificationType type;
    private UUID referenceId;
    private String referenceType;
    private boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}
