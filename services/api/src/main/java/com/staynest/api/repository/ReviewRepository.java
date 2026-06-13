package com.staynest.api.repository;

import com.staynest.api.entity.Review;
import com.staynest.api.enums.ReviewerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByPropertyIdAndPublishedTrueOrderByCreatedAtDesc(UUID propertyId, Pageable pageable);

    Optional<Review> findByBookingIdAndReviewerType(UUID bookingId, ReviewerType reviewerType);

    boolean existsByBookingIdAndReviewerType(UUID bookingId, ReviewerType reviewerType);

    long countByPropertyIdAndPublishedTrue(UUID propertyId);
}
