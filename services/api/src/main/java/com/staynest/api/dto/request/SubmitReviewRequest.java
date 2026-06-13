package com.staynest.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SubmitReviewRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer overallRating;

    @Min(value = 1) @Max(value = 5)
    private Integer cleanlinessRating;

    @Min(value = 1) @Max(value = 5)
    private Integer accuracyRating;

    @Min(value = 1) @Max(value = 5)
    private Integer checkinRating;

    @Min(value = 1) @Max(value = 5)
    private Integer communicationRating;

    @Min(value = 1) @Max(value = 5)
    private Integer locationRating;

    @Min(value = 1) @Max(value = 5)
    private Integer valueRating;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
}
