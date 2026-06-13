package com.staynest.api.controller;

import com.staynest.api.dto.request.CreateAmenityRequest;
import com.staynest.api.dto.response.AmenityResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.AmenityService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
@Tag(name = "Amenities", description = "Amenity master list management")
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @Operation(summary = "List all available amenities (PUBLIC)")
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> getAllAmenities(HttpServletRequest request) {
        List<AmenityResponse> amenities = amenityService.getAllAmenities();
        return ResponseEntity.ok(ApiResponse.success(amenities, "Amenities retrieved", request.getRequestURI()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Add a new amenity to the master list (SUPER_ADMIN only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AmenityResponse>> addAmenity(
            @Valid @RequestBody CreateAmenityRequest createRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        AmenityResponse amenity = amenityService.addAmenity(createRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(amenity, "Amenity added", request.getRequestURI()));
    }
}
