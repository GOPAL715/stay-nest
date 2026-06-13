package com.staynest.api.controller;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.dto.response.PropertySummaryResponse;
import com.staynest.api.entity.User;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.service.AdminListingService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
@Tag(name = "Admin - Listing Moderation", description = "Listing review and moderation")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'SUPER_ADMIN')")
public class AdminListingController {

    private final AdminListingService adminListingService;

    @GetMapping
    @Operation(summary = "Get listings by status (paginated)")
    public ResponseEntity<ApiResponse<Page<PropertySummaryResponse>>> getListingsByStatus(
            @RequestParam PropertyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PropertySummaryResponse> listings = adminListingService.getListingsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(listings, status + " listings retrieved", request.getRequestURI()));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get listings pending review (paginated)")
    public ResponseEntity<ApiResponse<Page<PropertySummaryResponse>>> getPendingListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PropertySummaryResponse> listings =
                adminListingService.getListingsByStatus(PropertyStatus.PENDING, pageable);
        return ResponseEntity.ok(ApiResponse.success(listings, "Pending listings retrieved", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/approve")
    @Operation(summary = "Approve a listing (PENDING to ACTIVE)")
    public ResponseEntity<ApiResponse<PropertyResponse>> approveListing(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = adminListingService.approveListing(propertyId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Listing approved", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/reject")
    @Operation(summary = "Reject a listing (PENDING to DRAFT)")
    public ResponseEntity<ApiResponse<PropertyResponse>> rejectListing(
            @PathVariable UUID propertyId,
            @Valid @RequestBody ModerationActionRequest actionRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = adminListingService.rejectListing(propertyId, actionRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Listing rejected", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/request-changes")
    @Operation(summary = "Request changes on a listing (back to DRAFT with reason)")
    public ResponseEntity<ApiResponse<PropertyResponse>> requestChanges(
            @PathVariable UUID propertyId,
            @Valid @RequestBody ModerationActionRequest actionRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = adminListingService.requestChanges(propertyId, actionRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Changes requested", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/suspend")
    @Operation(summary = "Suspend an active listing")
    public ResponseEntity<ApiResponse<PropertyResponse>> suspendListing(
            @PathVariable UUID propertyId,
            @Valid @RequestBody ModerationActionRequest actionRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = adminListingService.suspendListing(propertyId, actionRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Listing suspended", request.getRequestURI()));
    }
}
