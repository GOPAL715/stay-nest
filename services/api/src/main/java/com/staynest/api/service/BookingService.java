package com.staynest.api.service;

import com.staynest.api.dto.request.CreateBookingRequest;
import com.staynest.api.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, UUID guestId);

    BookingResponse confirmBooking(UUID bookingId, UUID hostId);

    BookingResponse cancelBooking(UUID bookingId, UUID actorId, String reason);

    BookingResponse getBookingById(UUID bookingId, UUID actorId);

    Page<BookingResponse> getMyTrips(UUID guestId, Pageable pageable);

    Page<BookingResponse> getHostBookings(UUID hostId, Pageable pageable);

    Page<BookingResponse> adminGetAllBookings(Pageable pageable);

    BookingResponse adminCancelBooking(UUID bookingId, String reason, UUID adminId);
}
