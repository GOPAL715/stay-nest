package com.staynest.api.service.impl;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.AuthResponse;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.RefreshToken;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.AuthenticationException;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.factory.UserFactory;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.RefreshTokenRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.security.JwtService;
import com.staynest.api.service.AuthService;
import com.staynest.api.service.EmailService;
import com.staynest.api.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserFactory userFactory;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    @Value("${app.auth.email-verification-required:true}")
    private boolean emailVerificationRequired;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.debug("Register attempt for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BusinessRuleException("An account with this email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Generate a 6-digit OTP and store it in the verificationToken field
        String otp = generateOtp();
        log.debug("OTP generated for: {}", request.getEmail());
        User user = userFactory.createNewUser(request, encodedPassword, otp);
        userRepository.save(user);
        log.debug("User saved to DB: id={}", user.getId());

        if (emailVerificationRequired) {
            log.debug("Sending OTP email to: {}", user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);
        } else {
            log.info("Email verification disabled (dev mode) — account auto-activated for: {}", user.getEmail());
        }
        log.info("New user registered: {} ({})", user.getEmail(), user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createAndSaveRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Transactional
    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or OTP"));

        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(request.getOtp())) {
            throw new AuthenticationException("Invalid OTP. Please check your email and try again.");
        }

        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new AuthenticationException("OTP has expired. Please register again to receive a new code.");
        }

        user.markEmailVerified();
        user.updateStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Email verified via OTP for user: {}", user.getEmail());
    }

    @Transactional
    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired verification token"));

        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new AuthenticationException("Verification token has expired. Please register again.");
        }

        user.markEmailVerified();
        user.updateStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (loginAttemptService.isLocked(email)) {
            throw new AuthenticationException("Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    loginAttemptService.recordFailure(email);
                    return new AuthenticationException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(email);
            throw new AuthenticationException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new AuthenticationException("Please verify your email address before logging in");
        }

        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.DELETED) {
            throw new AuthenticationException("Your account has been deactivated. Please contact support.");
        }

        loginAttemptService.reset(email);

        // Revoke existing refresh tokens
        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createAndSaveRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Transactional
    @Override
    public AuthResponse refreshToken(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired. Please log in again.");
        }

        // Rotate: revoke old, issue new
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createAndSaveRefreshToken(user);

        return buildAuthResponse(newAccessToken, newRefreshToken.getToken(), user);
    }

    @Transactional
    @Override
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        // Always return success to avoid email enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken, Instant.now().plusSeconds(3600)); // 1 hour
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("Password reset requested for: {}", email);
        });
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new AuthenticationException("Reset token has expired. Please request a new one.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearResetToken();
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.deleteByUser(user);
        log.info("Password reset successful for: {}", user.getEmail());
    }

    @Transactional
    @Override
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new AuthenticationException("No account found with this email"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessRuleException("This account is already verified. Please log in.");
        }

        String newOtp = generateOtp();
        user.setVerificationToken(newOtp, Instant.now().plusSeconds(10 * 60));
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), newOtp);
        log.info("OTP resent for user: {}", user.getEmail());
    }

    // --- Private helpers ---

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiry(Instant.now().plusMillis(refreshTokenExpiryMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshTokenValue, User user) {
        UserResponse userResponse = userMapper.toUserResponse(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiryMs())
                .user(userResponse)
                .build();
    }
}
