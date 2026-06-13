package com.staynest.api.service.impl;

import com.staynest.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.email.base-url}")
    private String baseUrl;

    @Async
    @Override
    public void sendOtpEmail(String to, String otp) {
        String subject = "Your StayNest verification code";
        String body = """
                Welcome to StayNest!
                
                Your email verification code is:
                
                    %s
                
                This code expires in 10 minutes.
                
                If you didn't create a StayNest account, you can safely ignore this email.
                
                — The StayNest Team
                """.formatted(otp);

        sendGenericEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your StayNest email address";
        String verifyUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;
        String body = """
                Welcome to StayNest!
                
                Please verify your email address by clicking the link below:
                %s
                
                This link expires in 24 hours.
                
                If you didn't create a StayNest account, you can safely ignore this email.
                
                — The StayNest Team
                """.formatted(verifyUrl);

        sendGenericEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset your StayNest password";
        String resetUrl = baseUrl + "/api/v1/auth/reset-password?token=" + token;
        String body = """
                We received a request to reset your StayNest password.
                
                Click the link below to reset your password:
                %s
                
                This link expires in 1 hour.
                
                If you didn't request a password reset, you can safely ignore this email.
                
                — The StayNest Team
                """.formatted(resetUrl);

        sendGenericEmail(to, subject, body);
    }

    @Override
    public void sendGenericEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
