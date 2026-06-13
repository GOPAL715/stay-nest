package com.staynest.api.controller;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.request.SubmitHostApplicationRequest;
import com.staynest.api.dto.response.HostApplicationResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.HostApplicationService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/host-applications")
@RequiredArgsConstructor
@Tag(name = "Host Applications", description = "Host application submission and review")
@SecurityRequirement(name = "bearerAuth")
public class HostApplicationController {

    private final HostApplicationService hostApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    @Operation(summary = "Submit a host application (GUEST only)")
    public ResponseEntity<ApiResponse<HostApplicationResponse>> submitApplication(
            @Valid @RequestBody SubmitHostApplicationRequest submitRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        HostApplicationResponse application = hostApplicationService.submitApplication(currentUser.getId(), submitRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(application, "Application submitted", request.getRequestURI()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(summary = "List all host applications (Admin)")
    public ResponseEntity<ApiResponse<Page<HostApplicationResponse>>> listApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HostApplicationResponse> applications = hostApplicationService.listApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success(applications, "Applications retrieved", request.getRequestURI()));
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(summary = "Get host application by ID (Admin)")
    public ResponseEntity<ApiResponse<HostApplicationResponse>> getApplicationById(
            @PathVariable UUID applicationId,
            HttpServletRequest request) {
        HostApplicationResponse application = hostApplicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success(application, "Application retrieved", request.getRequestURI()));
    }

    @PatchMapping("/{applicationId}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(summary = "Approve a host application (upgrades role to HOST)")
    public ResponseEntity<ApiResponse<HostApplicationResponse>> approveApplication(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        HostApplicationResponse application = hostApplicationService.approveApplication(applicationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(application, "Application approved", request.getRequestURI()));
    }

    @PatchMapping("/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(summary = "Reject a host application")
    public ResponseEntity<ApiResponse<HostApplicationResponse>> rejectApplication(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ModerationActionRequest actionRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        HostApplicationResponse application = hostApplicationService.rejectApplication(applicationId, actionRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(application, "Application rejected", request.getRequestURI()));
    }
}
