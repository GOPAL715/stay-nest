package com.staynest.api.controller;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.*;
import com.staynest.api.entity.User;
import com.staynest.api.service.PropertyService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property listing management and search")
public class PropertyController {

    private final PropertyService propertyService;

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping
    @Operation(summary = "Search properties with optional filters",
               description = "Returns paginated active property listings. All filters are optional.")
    public ResponseEntity<ApiResponse<Page<PropertySummaryResponse>>> searchProperties(
            @Parameter(description = "City to search in") @RequestParam(required = false) String city,
            @Parameter(description = "Check-in date (YYYY-MM-DD)") @RequestParam(required = false) LocalDate checkIn,
            @Parameter(description = "Check-out date (YYYY-MM-DD)") @RequestParam(required = false) LocalDate checkOut,
            @Parameter(description = "Number of guests") @RequestParam(required = false) Integer numGuests,
            @Parameter(description = "Minimum price per night (paise)") @RequestParam(required = false) Long minPrice,
            @Parameter(description = "Maximum price per night (paise)") @RequestParam(required = false) Long maxPrice,
            @Parameter(description = "Sort order: newest|price_asc|price_desc") @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {

        PropertySearchRequest searchRequest = PropertySearchRequest.builder()
                .city(city)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .numGuests(numGuests)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<PropertySummaryResponse> results = propertyService.searchProperties(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Properties retrieved", request.getRequestURI()));
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get full property detail by ID")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(
            @PathVariable UUID propertyId,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.getPropertyById(propertyId);
        return ResponseEntity.ok(ApiResponse.success(property, "Property retrieved", request.getRequestURI()));
    }

    @GetMapping("/{propertyId}/availability")
    @Operation(summary = "Get availability blocks for a property")
    public ResponseEntity<ApiResponse<List<PropertyAvailabilityResponse>>> getAvailability(
            @PathVariable UUID propertyId,
            HttpServletRequest request) {
        List<PropertyAvailabilityResponse> availability = propertyService.getAvailability(propertyId);
        return ResponseEntity.ok(ApiResponse.success(availability, "Availability retrieved", request.getRequestURI()));
    }

    // ==================== HOST ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Create a new property listing (HOST only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody CreatePropertyRequest createRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.createListing(createRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(property, "Property listing created", request.getRequestURI()));
    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Update a property listing (HOST owner only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdatePropertyRequest updateRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.updateListing(propertyId, updateRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Property updated", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/submit")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Submit listing for review (DRAFT → PENDING)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> submitForReview(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.submitForReview(propertyId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Listing submitted for review", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/photos")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Upload photo URLs to a property", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> addPhotos(
            @PathVariable UUID propertyId,
            @RequestBody List<String> photoUrls,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.addPhotos(propertyId, photoUrls, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Photos added", request.getRequestURI()));
    }

    @PatchMapping("/{propertyId}/amenities")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Update property amenities (replaces all)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyResponse>> updateAmenities(
            @PathVariable UUID propertyId,
            @RequestBody Set<UUID> amenityIds,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyResponse property = propertyService.updateAmenities(propertyId, amenityIds, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(property, "Amenities updated", request.getRequestURI()));
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Get host's own listings (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<PropertySummaryResponse>>> getMyListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PropertySummaryResponse> listings = propertyService.getMyListings(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(listings, "My listings retrieved", request.getRequestURI()));
    }

    @PostMapping("/{propertyId}/availability/block")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Block a date range for a property", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PropertyAvailabilityResponse>> blockAvailability(
            @PathVariable UUID propertyId,
            @Valid @RequestBody BlockAvailabilityRequest blockRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PropertyAvailabilityResponse block = propertyService.blockAvailability(propertyId, blockRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(block, "Dates blocked", request.getRequestURI()));
    }

    @DeleteMapping("/{propertyId}/availability/{blockId}")
    @PreAuthorize("hasRole('HOST')")
    @Operation(summary = "Unblock a date range", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> unblockAvailability(
            @PathVariable UUID propertyId,
            @PathVariable UUID blockId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        propertyService.unblockAvailability(propertyId, blockId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Dates unblocked", request.getRequestURI()));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasAnyRole('HOST', 'PROPERTY_MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Delete (soft) a property listing", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                               a.getAuthority().equals("ROLE_PROPERTY_MANAGER"));
        propertyService.deleteProperty(propertyId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Property deleted", request.getRequestURI()));
    }
}
