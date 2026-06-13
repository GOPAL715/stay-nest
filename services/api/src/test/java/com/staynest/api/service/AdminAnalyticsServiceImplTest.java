package com.staynest.api.service;

import com.staynest.api.dto.response.AdminKpiResponse;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PropertyRepository;
import com.staynest.api.service.impl.AdminAnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private AdminAnalyticsServiceImpl adminAnalyticsService;

    @Test
    void getKpis_returnsAggregatedMetrics() {
        given(propertyRepository.countByStatus(PropertyStatus.ACTIVE)).willReturn(42L);
        given(propertyRepository.countByStatus(PropertyStatus.PENDING)).willReturn(5L);
        given(bookingRepository.countByCreatedAtBetween(any(), any())).willReturn(18L);
        given(bookingRepository.sumPlatformFeeByStatusInAndCreatedAtBetween(
                eq(List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED)), any(), any()))
                .willReturn(2500000L);

        AdminKpiResponse kpis = adminAnalyticsService.getKpis();

        assertThat(kpis.getActiveListings()).isEqualTo(42L);
        assertThat(kpis.getPendingModeration()).isEqualTo(5L);
        assertThat(kpis.getBookingsThisMonth()).isEqualTo(18L);
        assertThat(kpis.getPlatformRevenuePaise()).isEqualTo(2500000L);
        assertThat(kpis.getPlatformRevenueInr()).isEqualTo("25000.00");
    }

    @Test
    void getMonthlyRevenue_returnsRequestedMonths() {
        given(bookingRepository.sumPlatformFeeByStatusInAndCreatedAtBetween(anyList(), any(), any()))
                .willReturn(100000L);
        given(bookingRepository.countByCreatedAtBetween(any(), any())).willReturn(3L);

        assertThat(adminAnalyticsService.getMonthlyRevenue(3)).hasSize(3);
    }
}
