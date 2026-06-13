package com.staynest.api.entity;

import com.staynest.api.enums.HostApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "host_applications")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HostApplication extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private HostApplicationStatus status;

    @Column(name = "motivation", columnDefinition = "TEXT")
    private String motivation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    // --- Mutators ---

    public void approve(User reviewer) {
        this.status = HostApplicationStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
    }

    public void reject(User reviewer, String notes) {
        this.status = HostApplicationStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewNotes = notes;
        this.reviewedAt = Instant.now();
    }
}
