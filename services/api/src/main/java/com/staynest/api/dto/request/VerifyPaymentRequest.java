package com.staynest.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;
    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;
    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
