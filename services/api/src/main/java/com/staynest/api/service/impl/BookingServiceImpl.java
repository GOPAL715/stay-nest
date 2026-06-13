package com.staynest.api.service.impl;

import com.staynest.api.dto.request.CreateBookingRequest;
import com.staynest.api.dto.response.BookingResponse;
import com.staynest.api.entity.*;
import com.staynest.api.enums.*;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ConflictException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.factory.BookingFactory;
import com.staynest.api.mapper.BookingMapper;
import com.staynest.api.repository.*;
import com.staynest.api.service.BookingService;
import com.staynest.api.service.PriceCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyAvailabilityRepository availabilityRepository;
    private final BookingFactory bookingFactory;
    private final BookingMapper bookingMapper;
    private final PriceCalculationService priceCalculationService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, UUID guestId) {
        // 1. Validate dates
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BusinessRuleException("Check-out date must be after check-in date");
        }

        // 2. Load property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new BusinessRuleException("This property is not available for booking");
        }

        // 3. Load guest
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));

        // 4. Ensure guest is not booking own property
        if (Objects.equals(property.getHost().getId(), guestId)) {
            throw new BusinessRuleException("You cannot book your own property");
        }

        // 5. Check guest capacity
        if (request.getNumGuests() > property.getMaxGuests()) {
            throw new BusinessRuleException("This property accommodates maximum " + property.getMaxGuests() + " guests");
        }

        // 6. Check for date conflicts (bookings)
        boolean hasConflict = bookingRepository.existsConflictingBooking(
                property.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        if (hasConflict) {
            throw new ConflictException("These dates are not available for this property");
        }

        // 7. Check availability blocks
        List<PropertyAvailability> blocks = availabilityRepository.findOverlapping(
                property.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!blocks.isEmpty()) {
            throw new ConflictException("These dates are blocked by the host");
        }

        // 8. Create booking via factory
        User host = property.getHost();
        var pricing = priceCalculationService.calculate(property, request.getCheckInDate(), request.getCheckOutDate());
        Booking booking = bookingFactory.createBooking(request, property, guest, host, pricing);

        // 9. Auto-confirm if INSTANT_BOOK
        if (property.getBookingMode() == BookingMode.INSTANT_BOOK) {
            booking.confirm();
            // Create an availability block to prevent double-booking
            PropertyAvailability block = PropertyAvailability.builder()
                    .property(property)
                    .startDate(request.getCheckInDate())
                    .endDate(request.getCheckOutDate())
                    .reason(AvailabilityBlockReason.BOOKED)
                    .build();
            availabilityRepository.save(block);
        }

        bookingRepository.save(booking);
        log.info("Booking created [{}] for property [{}] by guest [{}], status={}",
                booking.getId(), property.getId(), guestId, booking.getStatus());

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, UUID hostId) {
        Booking booking = getBookingById(bookingId);

        if (!Objects.equals(booking.getHost().getId(), hostId)) {
            throw new AccessDeniedException("You are not the host for this booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING bookings can be confirmed. Current: " + booking.getStatus());
        }

        booking.confirm();

        // Create availability block
        PropertyAvailability block = PropertyAvailability.builder()
                .property(booking.getProperty())
                .startDate(booking.getCheckInDate())
                .endDate(booking.getCheckOutDate())
                .reason(AvailabilityBlockReason.BOOKED)
                .build();
        availabilityRepository.save(block);
        bookingRepository.save(booking);

        log.info("Booking [{}] confirmed by host [{}]", bookingId, hostId);
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, UUID actorId, String reason) {
        Booking booking = getBookingById(bookingId);

        // Only guest, host, or admin can cancel
        boolean isGuest = Objects.equals(booking.getGuest().getId(), actorId);
        boolean isHost = Objects.equals(booking.getHost().getId(), actorId);

        if (!isGuest && !isHost) {
            // Could be admin — allow
            userRepository.findById(actorId)
                    .orElseThrow(() -> new AccessDeniedException("Not authorized to cancel this booking"));
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessRuleException("Completed bookings cannot be cancelled");
        }

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));
        booking.cancel(actor, reason);

        // Remove the BOOKED availability block if it exists
        List<PropertyAvailability> blocks = availabilityRepository.findOverlapping(
                booking.getProperty().getId(), booking.getCheckInDate(), booking.getCheckOutDate());
        blocks.stream()
                .filter(b -> b.getReason() == AvailabilityBlockReason.BOOKED)
                .forEach(availabilityRepository::delete);

        bookingRepository.save(booking);
        log.info("Booking [{}] cancelled by actor [{}]: {}", bookingId, actorId, reason);
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId, UUID actorId) {
        Booking booking = getBookingById(bookingId);

        boolean isGuest = Objects.equals(booking.getGuest().getId(), actorId);
        boolean isHost  = Objects.equals(booking.getHost().getId(), actorId);

        if (!isGuest && !isHost) {
            // Check if admin/support — allow if so (caller should have verified role)
            // For now, disallow — admin endpoints use adminGetAllBookings
            throw new AccessDeniedException("You do not have access to this booking");
        }

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyTrips(UUID guestId, Pageable pageable) {
        return bookingRepository.findByGuestIdOrderByCreatedAtDesc(guestId, pageable)
                .map(bookingMapper::toBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getHostBookings(UUID hostId, Pageable pageable) {
        return bookingRepository.findByHostIdOrderByCreatedAtDesc(hostId, pageable)
                .map(bookingMapper::toBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> adminGetAllBookings(Pageable pageable) {
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(bookingMapper::toBookingResponse);
    }

    @Override
    @Transactional
    public BookingResponse adminCancelBooking(UUID bookingId, String reason, UUID adminId) {
        return cancelBooking(bookingId, adminId, reason);
    }

    // --- Private helpers ---

    private Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }
}
