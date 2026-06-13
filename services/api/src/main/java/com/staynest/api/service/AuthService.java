package com.staynest.api.service;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    void verifyEmail(String token);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(String email);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
