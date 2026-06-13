package com.staynest.api.repository;

import com.staynest.api.entity.Booking;
import com.staynest.api.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByGuestIdOrderByCreatedAtDesc(UUID guestId, Pageable pageable);

    Page<Booking> findByHostIdOrderByCreatedAtDesc(UUID hostId, Pageable pageable);

    Page<Booking> findByPropertyIdOrderByCreatedAtDesc(UUID propertyId, Pageable pageable);

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Booking> findByPropertyIdAndStatus(UUID propertyId, BookingStatus status);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.property.id = :propertyId
              AND b.status IN :statuses
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    boolean existsConflictingBooking(
            @Param("propertyId") UUID propertyId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    long countByCreatedAtBetween(Instant start, Instant end);

    Page<Booking> findByStatusInOrderByCheckOutDateDesc(List<BookingStatus> statuses, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(b.platformFee), 0) FROM Booking b
            WHERE b.status IN :statuses
              AND b.createdAt >= :start
              AND b.createdAt < :end
            """)
    long sumPlatformFeeByStatusInAndCreatedAtBetween(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end);

    long countByHostIdAndStatus(UUID hostId, BookingStatus status);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.host.id = :hostId
              AND b.status IN :statuses
              AND b.checkInDate >= :fromDate
              AND b.checkInDate <= :toDate
            """)
    long countUpcomingCheckInsForHost(
            @Param("hostId") UUID hostId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount - b.platformFee - b.taxes), 0) FROM Booking b
            WHERE b.host.id = :hostId
              AND b.status IN :statuses
              AND b.createdAt >= :start
              AND b.createdAt < :end
            """)
    long sumHostEarningsForMonth(
            @Param("hostId") UUID hostId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
