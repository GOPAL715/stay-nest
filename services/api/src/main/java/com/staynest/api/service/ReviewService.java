package com.staynest.api.service;

import com.staynest.api.dto.request.HostResponseRequest;
import com.staynest.api.dto.request.SubmitReviewRequest;
import com.staynest.api.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse submitReview(SubmitReviewRequest request, UUID reviewerId);

    ReviewResponse addHostResponse(UUID reviewId, HostResponseRequest request, UUID hostId);

    Page<ReviewResponse> getPropertyReviews(UUID propertyId, Pageable pageable);

    ReviewResponse getReviewById(UUID reviewId);
}
