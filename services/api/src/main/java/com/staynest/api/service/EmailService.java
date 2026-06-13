package com.staynest.api.service;

public interface EmailService {

    void sendVerificationEmail(String to, String token);

    void sendOtpEmail(String to, String otp);

    void sendPasswordResetEmail(String to, String token);

    void sendGenericEmail(String to, String subject, String body);
}
