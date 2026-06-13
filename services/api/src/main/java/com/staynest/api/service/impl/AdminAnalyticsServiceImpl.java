package com.staynest.api.service.impl;

import com.staynest.api.dto.response.AdminKpiResponse;
import com.staynest.api.dto.response.MonthlyRevenueResponse;
import com.staynest.api.dto.response.PayoutResponse;
import com.staynest.api.entity.Booking;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PropertyRepository;
import com.staynest.api.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminKpiResponse getKpis() {
        YearMonth currentMonth = YearMonth.now();
        Instant monthStart = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        long activeListings = propertyRepository.countByStatus(PropertyStatus.ACTIVE);
        long pendingModeration = propertyRepository.countByStatus(PropertyStatus.PENDING);
        long bookingsThisMonth = bookingRepository.countByCreatedAtBetween(monthStart, monthEnd);
        long platformRevenuePaise = bookingRepository.sumPlatformFeeByStatusInAndCreatedAtBetween(
                List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED),
                monthStart,
                monthEnd
        );

        return AdminKpiResponse.builder()
                .activeListings(activeListings)
                .bookingsThisMonth(bookingsThisMonth)
                .pendingModeration(pendingModeration)
                .platformRevenuePaise(platformRevenuePaise)
                .platformRevenueInr(formatInr(platformRevenuePaise))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> getMonthlyRevenue(int months) {
        int period = Math.max(1, Math.min(months, 24));
        List<MonthlyRevenueResponse> results = new ArrayList<>();
        YearMonth cursor = YearMonth.now();

        for (int i = 0; i < period; i++) {
            Instant start = cursor.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = cursor.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            long feePaise = bookingRepository.sumPlatformFeeByStatusInAndCreatedAtBetween(
                    List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED),
                    start,
                    end
            );
            long count = bookingRepository.countByCreatedAtBetween(start, end);

            results.add(MonthlyRevenueResponse.builder()
                    .year(cursor.getYear())
                    .month(cursor.getMonthValue())
                    .monthLabel(cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + cursor.getYear())
                    .platformFeePaise(feePaise)
                    .platformFeeInr(formatInr(feePaise))
                    .bookingCount(count)
                    .build());

            cursor = cursor.minusMonths(1);
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayouts(Pageable pageable) {
        return bookingRepository.findByStatusInOrderByCheckOutDateDesc(
                        List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED),
                        pageable
                )
                .map(this::toPayoutResponse);
    }

    private PayoutResponse toPayoutResponse(Booking booking) {
        long hostPayout = booking.getTotalAmount() - booking.getPlatformFee() - booking.getTaxes();
        String hostName = booking.getHost().getFirstName() + " " + booking.getHost().getLastName();

        return PayoutResponse.builder()
                .bookingId(booking.getId())
                .hostId(booking.getHost().getId())
                .hostName(hostName)
                .propertyTitle(booking.getProperty().getTitle())
                .hostPayoutPaise(hostPayout)
                .hostPayoutInr(formatInr(hostPayout))
                .checkOutDate(booking.getCheckOutDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                .status(booking.getStatus().name())
                .build();
    }

    private static String formatInr(long paise) {
        BigDecimal rupees = BigDecimal.valueOf(paise, 2);
        return rupees.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
