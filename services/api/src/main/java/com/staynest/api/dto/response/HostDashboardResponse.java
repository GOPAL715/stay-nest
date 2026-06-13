package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Host dashboard KPI summary")
public class HostDashboardResponse {

    private long activeListings;
    private long upcomingCheckIns;
    private long pendingBookingRequests;
    private long earningsThisMonthPaise;
    private String earningsThisMonthInr;
}
