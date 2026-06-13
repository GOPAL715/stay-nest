package com.staynest.api.service.impl;

import com.staynest.api.dto.request.SendMessageRequest;
import com.staynest.api.dto.response.MessageResponse;
import com.staynest.api.entity.Booking;
import com.staynest.api.entity.Message;
import com.staynest.api.entity.User;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.MessageRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID bookingId, SendMessageRequest request, UUID senderId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Only guest or host of this booking can message
        boolean isGuest = Objects.equals(booking.getGuest().getId(), senderId);
        boolean isHost  = Objects.equals(booking.getHost().getId(), senderId);
        if (!isGuest && !isHost) {
            throw new AccessDeniedException("You are not a participant of this booking");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .booking(booking)
                .sender(sender)
                .content(request.getContent())
                .read(false)
                .build();

        messageRepository.save(message);
        log.info("Message sent in booking [{}] by [{}]", bookingId, senderId);
        return toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversation(UUID bookingId, UUID actorId, Pageable pageable) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isGuest = Objects.equals(booking.getGuest().getId(), actorId);
        boolean isHost  = Objects.equals(booking.getHost().getId(), actorId);
        if (!isGuest && !isHost) {
            throw new AccessDeniedException("You are not a participant of this booking");
        }

        return messageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void markConversationRead(UUID bookingId, UUID readerId) {
        List<Message> unread = messageRepository.findByBookingIdAndReadFalseAndSenderIdNot(bookingId, readerId);
        unread.forEach(Message::markRead);
        messageRepository.saveAll(unread);
    }

    // --- Mapper ---

    private MessageResponse toResponse(Message message) {
        String senderName = message.getSender().getFirstName() + " " + message.getSender().getLastName();
        return MessageResponse.builder()
                .id(message.getId())
                .bookingId(message.getBooking().getId())
                .senderId(message.getSender().getId())
                .senderName(senderName)
                .content(message.getContent())
                .read(message.isRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
