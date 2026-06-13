package com.staynest.api.service;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.AuthResponse;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.RefreshToken;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.AuthenticationException;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.factory.UserFactory;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.RefreshTokenRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.security.JwtService;
import com.staynest.api.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // --- Mocks ---

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserFactory userFactory;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordEncoder passwordEncoder;

    // --- Subject under test ---

    @InjectMocks
    private AuthServiceImpl authService;

    // --- Constants ---

    private static final String TEST_EMAIL       = "john.doe@example.com";
    private static final String TEST_PASSWORD    = "SecurePass@123";
    private static final String TEST_ACCESS_TOKEN  = "access-token-value";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-value";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604800000L);
        ReflectionTestUtils.setField(authService, "emailVerificationRequired", true);
    }

    // =========================================================================
    // Helper factories
    // =========================================================================

    private User mockUser(UserStatus status) {
        return User.builder()
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .firstName("John")
                .lastName("Doe")
                .phone("+919876543210")
                .role(UserRole.GUEST)
                .status(status)
                .emailVerified(status == UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();
    }

    private RefreshToken mockRefreshToken(User user, boolean revoked, boolean expired) {
        Instant expiry = expired ? Instant.now().minusSeconds(60) : Instant.now().plusSeconds(604800);
        return RefreshToken.builder()
                .user(user)
                .token(TEST_REFRESH_TOKEN)
                .expiry(expiry)
                .revoked(revoked)
                .build();
    }

    private UserResponse mockUserResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .phone("+919876543210")
                .build();
    }

    private LoginRequest loginRequest() {
        return LoginRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
    }

    // =========================================================================
    // register
    // =========================================================================

    @Test
    void register_success_savesUserSendsVerificationEmailAndReturnsAuthResponse() {
        // arrange
        RegisterRequest request = registerRequest();
        User user = mockUser(UserStatus.UNVERIFIED);
        RefreshToken savedRefreshToken = mockRefreshToken(user, false, false);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("$2a$10$hashed");
        when(userFactory.createNewUser(eq(request), anyString(), anyString())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(TEST_ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(user)).thenReturn(mockUserResponse());

        // act
        AuthResponse response = authService.register(request);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository).save(user);
        verify(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
        verify(jwtService).generateAccessToken(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_duplicateEmail_throwsBusinessRuleException() {
        // arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // act & assert
        assertThrows(BusinessRuleException.class, () -> authService.register(registerRequest()));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    // =========================================================================
    // verifyEmail
    // =========================================================================

    @Test
    void verifyEmail_success_marksEmailVerifiedAndSetsStatusActive() {
        // arrange
        User user = User.builder()
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.GUEST)
                .status(UserStatus.UNVERIFIED)
                .emailVerified(false)
                .verificationToken("valid-token")
                .verificationTokenExpiry(Instant.now().plusSeconds(3600))
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // act
        authService.verifyEmail("valid-token");

        // assert
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_invalidToken_throwsAuthenticationException() {
        // arrange
        when(userRepository.findByVerificationToken("bad-token")).thenReturn(Optional.empty());

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.verifyEmail("bad-token"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_expiredToken_throwsAuthenticationException() {
        // arrange
        User user = User.builder()
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.GUEST)
                .status(UserStatus.UNVERIFIED)
                .emailVerified(false)
                .verificationToken("expired-token")
                .verificationTokenExpiry(Instant.now().minusSeconds(3600)) // already expired
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.verifyEmail("expired-token"));

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // login
    // =========================================================================

    @Test
    void login_success_deletesOldTokensAndReturnsAuthResponse() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        RefreshToken savedRefreshToken = mockRefreshToken(user, false, false);

        when(loginAttemptService.isLocked(TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn(TEST_ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(user)).thenReturn(mockUserResponse());

        // act
        AuthResponse response = authService.login(loginRequest());

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        verify(refreshTokenRepository).deleteByUser(user);
        verify(loginAttemptService).reset(TEST_EMAIL);
    }

    @Test
    void login_accountLocked_throwsAuthenticationException() {
        // arrange
        when(loginAttemptService.isLocked(TEST_EMAIL)).thenReturn(true);

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest()));

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_wrongPassword_recordsFailureAndThrowsAuthenticationException() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);

        when(loginAttemptService.isLocked(TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, user.getPasswordHash())).thenReturn(false);

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest()));

        verify(loginAttemptService).recordFailure(TEST_EMAIL);
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_unverifiedUser_throwsAuthenticationException() {
        // arrange
        User user = mockUser(UserStatus.UNVERIFIED);

        when(loginAttemptService.isLocked(TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, user.getPasswordHash())).thenReturn(true);

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest()));

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_inactiveUser_throwsAuthenticationException() {
        // arrange
        User user = mockUser(UserStatus.INACTIVE);

        when(loginAttemptService.isLocked(TEST_EMAIL)).thenReturn(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, user.getPasswordHash())).thenReturn(true);

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest()));

        verify(jwtService, never()).generateAccessToken(any());
    }

    // =========================================================================
    // refreshToken
    // =========================================================================

    @Test
    void refreshToken_success_revokesOldAndReturnsNewAuthResponse() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        RefreshToken existingToken = mockRefreshToken(user, false, false);
        RefreshToken newRefreshToken = mockRefreshToken(user, false, false);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(existingToken));
        // first save() persists the revoked old token; second save() persists the new token
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(existingToken)
                .thenReturn(newRefreshToken);
        when(jwtService.generateAccessToken(user)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(userMapper.toUserResponse(user)).thenReturn(mockUserResponse());

        // act
        AuthResponse response = authService.refreshToken(TEST_REFRESH_TOKEN);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(existingToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_revokedToken_throwsAuthenticationException() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        RefreshToken revokedToken = mockRefreshToken(user, true, false);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(revokedToken));

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.refreshToken(TEST_REFRESH_TOKEN));

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_expiredToken_deletesTokenAndThrowsAuthenticationException() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        RefreshToken expiredToken = mockRefreshToken(user, false, true);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(expiredToken));

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.refreshToken(TEST_REFRESH_TOKEN));

        verify(refreshTokenRepository).delete(expiredToken);
        verify(jwtService, never()).generateAccessToken(any());
    }

    // =========================================================================
    // logout
    // =========================================================================

    @Test
    void logout_success_revokesToken() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        RefreshToken token = mockRefreshToken(user, false, false);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(token)).thenReturn(token);

        // act
        authService.logout(TEST_REFRESH_TOKEN);

        // assert
        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_tokenNotFound_noExceptionThrown() {
        // arrange
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        // act & assert — should complete without throwing
        authService.logout("unknown-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    // =========================================================================
    // forgotPassword
    // =========================================================================

    @Test
    void forgotPassword_userExists_setsResetTokenAndSendsEmail() {
        // arrange
        User user = mockUser(UserStatus.ACTIVE);
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email(TEST_EMAIL).build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // act
        authService.forgotPassword(request);

        // assert
        assertThat(user.getResetToken()).isNotNull();
        verify(emailService).sendPasswordResetEmail(eq(TEST_EMAIL), anyString());
        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_userNotFound_noExceptionThrown() {
        // arrange — anti-enumeration: must not throw even when user absent
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email("unknown@example.com").build();

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // act & assert
        authService.forgotPassword(request);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // resetPassword
    // =========================================================================

    @Test
    void resetPassword_success_updatesPasswordAndClearsResetToken() {
        // arrange
        String resetToken = "valid-reset-token";
        String newPassword = "NewSecurePass@456";

        User user = User.builder()
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$oldhash")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .resetToken(resetToken)
                .resetTokenExpiry(Instant.now().plusSeconds(3600))
                .failedLoginAttempts(0)
                .build();

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(resetToken)
                .newPassword(newPassword)
                .build();

        when(userRepository.findByResetToken(resetToken)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newhash");
        when(userRepository.save(user)).thenReturn(user);

        // act
        authService.resetPassword(request);

        // assert
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newhash");
        assertThat(user.getResetToken()).isNull();
        assertThat(user.getResetTokenExpiry()).isNull();
        verify(userRepository).save(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void resetPassword_expiredToken_throwsAuthenticationException() {
        // arrange
        String resetToken = "expired-reset-token";

        User user = User.builder()
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$oldhash")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .resetToken(resetToken)
                .resetTokenExpiry(Instant.now().minusSeconds(3600)) // already expired
                .failedLoginAttempts(0)
                .build();

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(resetToken)
                .newPassword("NewSecurePass@456")
                .build();

        when(userRepository.findByResetToken(resetToken)).thenReturn(Optional.of(user));

        // act & assert
        assertThrows(AuthenticationException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
