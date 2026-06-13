package com.staynest.api.controller;

import com.staynest.api.dto.response.AdminKpiResponse;
import com.staynest.api.dto.response.MonthlyRevenueResponse;
import com.staynest.api.dto.response.PayoutResponse;
import com.staynest.api.service.AdminAnalyticsService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin - Analytics", description = "Revenue and KPI analytics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/kpis")
    @Operation(summary = "Get dashboard KPI metrics")
    public ResponseEntity<ApiResponse<AdminKpiResponse>> getKpis(HttpServletRequest request) {
        AdminKpiResponse kpis = adminAnalyticsService.getKpis();
        return ResponseEntity.ok(ApiResponse.success(kpis, "KPIs retrieved", request.getRequestURI()));
    }

    @GetMapping("/revenue/monthly")
    @Operation(summary = "Get monthly platform revenue trend")
    public ResponseEntity<ApiResponse<List<MonthlyRevenueResponse>>> getMonthlyRevenue(
            @RequestParam(defaultValue = "6") int months,
            HttpServletRequest request) {
        List<MonthlyRevenueResponse> revenue = adminAnalyticsService.getMonthlyRevenue(months);
        return ResponseEntity.ok(ApiResponse.success(revenue, "Monthly revenue retrieved", request.getRequestURI()));
    }

    @GetMapping("/payouts")
    @Operation(summary = "Get host payout history (paginated)")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutResponse> payouts = adminAnalyticsService.getPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts, "Payouts retrieved", request.getRequestURI()));
    }

    @GetMapping("/payouts/export")
    @Operation(summary = "Export payout history as CSV")
    public ResponseEntity<byte[]> exportPayoutsCsv(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutResponse> payouts = adminAnalyticsService.getPayouts(pageable);

        StringBuilder csv = new StringBuilder("bookingId,hostName,propertyTitle,hostPayoutInr,status,checkOutDate\n");
        payouts.forEach(p -> csv.append(p.getBookingId()).append(',')
                .append(escapeCsv(p.getHostName())).append(',')
                .append(escapeCsv(p.getPropertyTitle())).append(',')
                .append(p.getHostPayoutInr()).append(',')
                .append(p.getStatus()).append(',')
                .append(p.getCheckOutDate()).append('\n'));

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payouts.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
