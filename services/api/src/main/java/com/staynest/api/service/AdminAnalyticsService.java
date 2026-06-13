package com.staynest.api.service;

import com.staynest.api.dto.response.AdminKpiResponse;
import com.staynest.api.dto.response.MonthlyRevenueResponse;
import com.staynest.api.dto.response.PayoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminAnalyticsService {

    AdminKpiResponse getKpis();

    List<MonthlyRevenueResponse> getMonthlyRevenue(int months);

    Page<PayoutResponse> getPayouts(Pageable pageable);
}
