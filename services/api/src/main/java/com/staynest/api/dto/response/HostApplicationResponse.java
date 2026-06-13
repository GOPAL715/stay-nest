package com.staynest.api.dto.response;

import com.staynest.api.enums.HostApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Host application response")
public class HostApplicationResponse {

    private UUID id;

    @Schema(description = "Applicant basic info")
    private UserResponse applicant;

    private HostApplicationStatus status;
    private String motivation;
    private String reviewNotes;
    private Instant reviewedAt;
    private Instant createdAt;

    @Schema(description = "Reviewer's basic info (if reviewed)")
    private UserResponse reviewedBy;
}
