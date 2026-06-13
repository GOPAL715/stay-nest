package com.staynest.api.dto.response;

import com.staynest.api.enums.ReviewerType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReviewResponse {

    private UUID id;
    private UUID bookingId;
    private UUID propertyId;
    private UUID reviewerId;
    private String reviewerName;
    private ReviewerType reviewerType;
    private int overallRating;
    private Integer cleanlinessRating;
    private Integer accuracyRating;
    private Integer checkinRating;
    private Integer communicationRating;
    private Integer locationRating;
    private Integer valueRating;
    private String comment;
    private String hostResponse;
    private Instant hostResponseAt;
    private boolean published;
    private Instant submittedAt;
    private Instant createdAt;
}
