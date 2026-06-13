package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Authentication response containing JWT tokens")
public class AuthResponse {

    @Schema(description = "JWT access token (short-lived)")
    private String accessToken;

    @Schema(description = "Refresh token (long-lived, for token rotation)")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiry in milliseconds", example = "900000")
    private long expiresIn;

    @Schema(description = "Authenticated user details")
    private UserResponse user;
}
