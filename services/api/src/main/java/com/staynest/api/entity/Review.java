package com.staynest.api.entity;

import com.staynest.api.enums.ReviewerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_type", nullable = false, length = 10)
    private ReviewerType reviewerType;

    @Column(name = "overall_rating", nullable = false)
    private int overallRating;

    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;

    @Column(name = "accuracy_rating")
    private Integer accuracyRating;

    @Column(name = "checkin_rating")
    private Integer checkinRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "location_rating")
    private Integer locationRating;

    @Column(name = "value_rating")
    private Integer valueRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "host_response", columnDefinition = "TEXT")
    private String hostResponse;

    @Column(name = "host_response_at")
    private Instant hostResponseAt;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    // --- Mutators ---

    public void publish() {
        this.published = true;
    }

    public void addHostResponse(String response) {
        this.hostResponse = response;
        this.hostResponseAt = Instant.now();
    }
}
