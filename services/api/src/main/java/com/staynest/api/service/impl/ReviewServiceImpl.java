package com.staynest.api.service.impl;

import com.staynest.api.dto.request.HostResponseRequest;
import com.staynest.api.dto.request.SubmitReviewRequest;
import com.staynest.api.dto.response.ReviewResponse;
import com.staynest.api.entity.Booking;
import com.staynest.api.entity.Review;
import com.staynest.api.entity.User;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.ReviewerType;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.ReviewRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse submitReview(SubmitReviewRequest request, UUID reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleException("Reviews can only be submitted for completed bookings");
        }

        // Determine reviewer type
        ReviewerType reviewerType;
        User reviewee;
        if (Objects.equals(booking.getGuest().getId(), reviewerId)) {
            reviewerType = ReviewerType.GUEST;
            reviewee = booking.getHost();
        } else if (Objects.equals(booking.getHost().getId(), reviewerId)) {
            reviewerType = ReviewerType.HOST;
            reviewee = booking.getGuest();
        } else {
            throw new AccessDeniedException("You are not a participant of this booking");
        }

        // Prevent duplicate reviews
        if (reviewRepository.existsByBookingIdAndReviewerType(booking.getId(), reviewerType)) {
            throw new BusinessRuleException("You have already submitted a review for this booking");
        }

        Review review = Review.builder()
                .booking(booking)
                .property(booking.getProperty())
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewerType(reviewerType)
                .overallRating(request.getOverallRating())
                .cleanlinessRating(request.getCleanlinessRating())
                .accuracyRating(request.getAccuracyRating())
                .checkinRating(request.getCheckinRating())
                .communicationRating(request.getCommunicationRating())
                .locationRating(request.getLocationRating())
                .valueRating(request.getValueRating())
                .comment(request.getComment())
                .published(false)
                .submittedAt(Instant.now())
                .build();

        reviewRepository.save(review);

        // Double-blind: publish both if counterpart has already submitted
        ReviewerType counterpartType = reviewerType == ReviewerType.GUEST ? ReviewerType.HOST : ReviewerType.GUEST;
        reviewRepository.findByBookingIdAndReviewerType(booking.getId(), counterpartType)
                .ifPresent(counterpart -> {
                    counterpart.publish();
                    review.publish();
                    reviewRepository.save(counterpart);
                });

        reviewRepository.save(review);
        log.info("Review submitted for booking [{}] by [{}] as {}", booking.getId(), reviewerId, reviewerType);
        return toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse addHostResponse(UUID reviewId, HostResponseRequest request, UUID hostId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!Objects.equals(review.getReviewee().getId(), hostId)) {
            throw new AccessDeniedException("Only the reviewee (host) can respond to this review");
        }
        if (review.getHostResponse() != null) {
            throw new BusinessRuleException("A response has already been added to this review");
        }

        review.addHostResponse(request.getResponse());
        reviewRepository.save(review);
        log.info("Host [{}] added response to review [{}]", hostId, reviewId);
        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPropertyReviews(UUID propertyId, Pageable pageable) {
        return reviewRepository.findByPropertyIdAndPublishedTrueOrderByCreatedAtDesc(propertyId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return toResponse(review);
    }

    // --- Mapper ---

    private ReviewResponse toResponse(Review review) {
        String reviewerName = review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName();
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .propertyId(review.getProperty().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(reviewerName)
                .reviewerType(review.getReviewerType())
                .overallRating(review.getOverallRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .accuracyRating(review.getAccuracyRating())
                .checkinRating(review.getCheckinRating())
                .communicationRating(review.getCommunicationRating())
                .locationRating(review.getLocationRating())
                .valueRating(review.getValueRating())
                .comment(review.getComment())
                .hostResponse(review.getHostResponse())
                .hostResponseAt(review.getHostResponseAt())
                .published(review.isPublished())
                .submittedAt(review.getSubmittedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
