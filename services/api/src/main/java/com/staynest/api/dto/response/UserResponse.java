package com.staynest.api.dto.response;

import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "User profile response")
public class UserResponse {

    @Schema(description = "User unique identifier")
    private UUID id;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Profile picture URL")
    private String profilePictureUrl;

    @Schema(description = "User role")
    private UserRole role;

    @Schema(description = "User account status")
    private UserStatus status;

    @Schema(description = "Whether email has been verified")
    private boolean emailVerified;

    @Schema(description = "Account creation timestamp")
    private Instant createdAt;
}
