package com.staynest.api.controller;

import com.staynest.api.dto.request.CancelBookingRequest;
import com.staynest.api.dto.request.CreateBookingRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking creation, management, and history")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'HOST')")
    @Operation(summary = "Create a new booking (GUEST only)")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest createRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        BookingResponse booking = bookingService.createBooking(createRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created", request.getRequestURI()));
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST')")
    @Operation(summary = "Get guest's own bookings (paginated)")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> trips = bookingService.getMyTrips(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(trips, "My trips retrieved", request.getRequestURI()));
    }

    @GetMapping("/host-bookings")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Get bookings for host's properties (paginated)")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getHostBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> bookings = bookingService.getHostBookings(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "Host bookings retrieved", request.getRequestURI()));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking detail (guest or host of the booking)")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        BookingResponse booking = bookingService.getBookingById(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved", request.getRequestURI()));
    }

    @PatchMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Confirm a Request-to-Book booking (HOST only)")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        BookingResponse booking = bookingService.confirmBooking(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking confirmed", request.getRequestURI()));
    }

    @PatchMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking (guest, host, or admin)")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CancelBookingRequest cancelRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        BookingResponse booking = bookingService.cancelBooking(bookingId, currentUser.getId(), cancelRequest.getReason());
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled", request.getRequestURI()));
    }
}
