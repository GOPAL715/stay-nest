package com.staynest.api.service;

import com.staynest.api.dto.request.SendMessageRequest;
import com.staynest.api.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageService {

    MessageResponse sendMessage(UUID bookingId, SendMessageRequest request, UUID senderId);

    Page<MessageResponse> getConversation(UUID bookingId, UUID actorId, Pageable pageable);

    void markConversationRead(UUID bookingId, UUID readerId);
}
