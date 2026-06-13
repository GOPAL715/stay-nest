package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Admin dashboard KPI metrics")
public class AdminKpiResponse {

    private long activeListings;
    private long bookingsThisMonth;
    private long pendingModeration;
    private long platformRevenuePaise;
    private String platformRevenueInr;
}
