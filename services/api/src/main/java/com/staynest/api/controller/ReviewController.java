package com.staynest.api.controller;

import com.staynest.api.dto.request.HostResponseRequest;
import com.staynest.api.dto.request.SubmitReviewRequest;
import com.staynest.api.dto.response.ReviewResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Submit and manage property reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('GUEST') or hasRole('HOST')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit a review for a completed booking (GUEST or HOST)")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest servletRequest) {
        ReviewResponse review = reviewService.submitReview(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Review submitted", servletRequest.getRequestURI()));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get a review by ID (public)")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable UUID reviewId,
            HttpServletRequest servletRequest) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review, "Review retrieved", servletRequest.getRequestURI()));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get paginated reviews for a property (public)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPropertyReviews(
            @PathVariable UUID propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews = reviewService.getPropertyReviews(propertyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Property reviews retrieved", servletRequest.getRequestURI()));
    }

    @PostMapping("/{reviewId}/response")
    @PreAuthorize("hasRole('HOST')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a public host response to a review (HOST only)")
    public ResponseEntity<ApiResponse<ReviewResponse>> addHostResponse(
            @PathVariable UUID reviewId,
            @Valid @RequestBody HostResponseRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest servletRequest) {
        ReviewResponse review = reviewService.addHostResponse(reviewId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(review, "Host response added", servletRequest.getRequestURI()));
    }
}
