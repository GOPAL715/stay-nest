package com.staynest.api.controller;

import com.staynest.api.dto.request.SendMessageRequest;
import com.staynest.api.dto.response.MessageResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.MessageService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Booking conversation thread messaging")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message in a booking thread (GUEST or HOST)")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable UUID bookingId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        MessageResponse response = messageService.sendMessage(bookingId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Message sent", httpRequest.getRequestURI()));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated conversation thread for a booking")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getConversation(
            @PathVariable UUID bookingId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        Page<MessageResponse> conversation = messageService.getConversation(bookingId, currentUser.getId(), pageable);
        return ResponseEntity.ok(
                ApiResponse.success(conversation, "Conversation retrieved", httpRequest.getRequestURI()));
    }

    @PatchMapping("/booking/{bookingId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all messages in a conversation as read")
    public ResponseEntity<ApiResponse<Void>> markConversationRead(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        messageService.markConversationRead(bookingId, currentUser.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Conversation marked as read", httpRequest.getRequestURI()));
    }
}
