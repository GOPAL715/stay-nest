package com.staynest.api.service;

import com.staynest.api.dto.response.HostDashboardResponse;

import java.util.UUID;

public interface HostDashboardService {

    HostDashboardResponse getDashboard(UUID hostId);
}
