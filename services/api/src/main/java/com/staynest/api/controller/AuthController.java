package com.staynest.api.controller;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.request.VerifyOtpRequest;
import com.staynest.api.dto.response.AuthResponse;
import com.staynest.api.service.AuthService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication — register, login, token refresh, email verification, password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration successful. Verification email sent."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation errors"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful. Please verify your email.", servletRequest.getRequestURI()));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP verification code to email")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam String email,
            HttpServletRequest request) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent to your email.", request.getRequestURI()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify email using 6-digit OTP sent to customer inbox")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified. Account activated."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired OTP")
    })
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest otpRequest,
            HttpServletRequest request) {
        authService.verifyOtp(otpRequest);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in.", request.getRequestURI()));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address using token from email link")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token,
            HttpServletRequest request) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in.", request.getRequestURI()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password, receive JWT pair")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful", request.getRequestURI()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and get new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest refreshRequest,
            HttpServletRequest request) {
        AuthResponse response = authService.refreshToken(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully", request.getRequestURI()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token (logout)")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest refreshRequest,
            HttpServletRequest request) {
        authService.logout(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", request.getRequestURI()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotRequest,
            HttpServletRequest request) {
        authService.forgotPassword(forgotRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "If an account exists with this email, a reset link has been sent.",
                request.getRequestURI()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetRequest,
            HttpServletRequest request) {
        authService.resetPassword(resetRequest);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful. Please log in with your new password.", request.getRequestURI()));
    }
}
