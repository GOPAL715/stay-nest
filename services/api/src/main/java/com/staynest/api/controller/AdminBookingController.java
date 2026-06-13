package com.staynest.api.controller;

import com.staynest.api.dto.request.CancelBookingRequest;
import com.staynest.api.dto.response.BookingResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.BookingService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin - Bookings", description = "Admin booking management")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "List all bookings (paginated) — Admin/Support")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> bookings = bookingService.adminGetAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "All bookings retrieved", request.getRequestURI()));
    }

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Admin cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> adminCancelBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CancelBookingRequest cancelRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        BookingResponse booking = bookingService.adminCancelBooking(bookingId, cancelRequest.getReason(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled by admin", request.getRequestURI()));
    }
}
