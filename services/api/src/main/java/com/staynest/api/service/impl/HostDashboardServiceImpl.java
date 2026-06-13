package com.staynest.api.service.impl;

import com.staynest.api.dto.response.HostDashboardResponse;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PropertyRepository;
import com.staynest.api.service.HostDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostDashboardServiceImpl implements HostDashboardService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public HostDashboardResponse getDashboard(UUID hostId) {
        YearMonth currentMonth = YearMonth.now();
        Instant monthStart = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        LocalDate today = LocalDate.now();
        LocalDate weekAhead = today.plusDays(7);

        long activeListings = propertyRepository.countByHostIdAndStatus(hostId, PropertyStatus.ACTIVE);
        long pendingRequests = bookingRepository.countByHostIdAndStatus(hostId, BookingStatus.PENDING);
        long upcomingCheckIns = bookingRepository.countUpcomingCheckInsForHost(
                hostId, today, weekAhead, List.of(BookingStatus.CONFIRMED));
        long earningsPaise = bookingRepository.sumHostEarningsForMonth(
                hostId,
                List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED),
                monthStart,
                monthEnd
        );

        return HostDashboardResponse.builder()
                .activeListings(activeListings)
                .upcomingCheckIns(upcomingCheckIns)
                .pendingBookingRequests(pendingRequests)
                .earningsThisMonthPaise(earningsPaise)
                .earningsThisMonthInr(formatInr(earningsPaise))
                .build();
    }

    private static String formatInr(long paise) {
        return BigDecimal.valueOf(paise, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
