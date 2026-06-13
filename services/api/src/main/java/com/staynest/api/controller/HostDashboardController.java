package com.staynest.api.controller;

import com.staynest.api.dto.response.HostDashboardResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.HostDashboardService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/host/dashboard")
@RequiredArgsConstructor
@Tag(name = "Host Dashboard", description = "Host operational dashboard KPIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('HOST')")
public class HostDashboardController {

    private final HostDashboardService hostDashboardService;

    @GetMapping
    @Operation(summary = "Get host dashboard KPIs")
    public ResponseEntity<ApiResponse<HostDashboardResponse>> getDashboard(
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        HostDashboardResponse dashboard = hostDashboardService.getDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Host dashboard retrieved", request.getRequestURI()));
    }
}
