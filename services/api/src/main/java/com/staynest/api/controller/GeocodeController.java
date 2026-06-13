package com.staynest.api.controller;

import com.staynest.api.dto.GeocodeAddress;
import com.staynest.api.dto.GeoCoordinates;
import com.staynest.api.dto.response.GeocodeResponse;
import com.staynest.api.exception.ApplicationException;
import com.staynest.api.service.GeocodingService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/geocode")
@RequiredArgsConstructor
@Tag(name = "Geocoding", description = "Address to coordinates lookup (OpenStreetMap Nominatim)")
public class GeocodeController {

    private final GeocodingService geocodingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('HOST', 'SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(
            summary = "Geocode an address to latitude/longitude",
            description = "Uses free OpenStreetMap Nominatim. Hosts can preview map pin placement while creating listings.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<GeocodeResponse>> geocodeAddress(
            @RequestParam String addressLine1,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam(required = false) String addressLine2,
            @RequestParam(required = false) String postalCode,
            HttpServletRequest request
    ) {
        GeocodeAddress address = GeocodeAddress.of(addressLine1, addressLine2, city, state, country, postalCode);
        GeoCoordinates coordinates = geocodingService.geocode(address)
                .orElseThrow(() -> new ApplicationException(
                        "Could not locate coordinates for that address. Try a simpler address or drag the pin on the map.",
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "GEOCODING_FAILED"));

        GeocodeResponse response = GeocodeResponse.builder()
                .latitude(coordinates.latitude())
                .longitude(coordinates.longitude())
                .displayName(coordinates.displayName())
                .approximate(coordinates.approximate())
                .build();

        String message = coordinates.approximate()
                ? "Approximate city location found. Drag the pin to your exact address."
                : "Address geocoded";

        return ResponseEntity.ok(ApiResponse.success(response, message, request.getRequestURI()));
    }
}
