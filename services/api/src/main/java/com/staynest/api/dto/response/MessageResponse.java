package com.staynest.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class MessageResponse {

    private UUID id;
    private UUID bookingId;
    private UUID senderId;
    private String senderName;
    private String content;
    private boolean read;
    private Instant readAt;
    private Instant createdAt;
}
