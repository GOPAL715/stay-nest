package com.staynest.api.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.staynest.api.dto.request.CreatePaymentOrderRequest;
import com.staynest.api.dto.request.VerifyPaymentRequest;
import com.staynest.api.dto.response.PaymentResponse;
import com.staynest.api.dto.response.RefundResponse;
import com.staynest.api.entity.Booking;
import com.staynest.api.entity.Payment;
import com.staynest.api.entity.Refund;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PaymentStatus;
import com.staynest.api.enums.RefundStatus;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PaymentRepository;
import com.staynest.api.repository.RefundRepository;
import com.staynest.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${app.payments.demo-mode:true}")
    private boolean demoMode;

    private static final String DEMO_SIGNATURE = "demo_signature";
    private static final String DEMO_ORDER_PREFIX = "order_demo_";

    // -----------------------------------------------------------------------
    // Create Razorpay order
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse createOrder(CreatePaymentOrderRequest request, UUID guestId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        assertGuestOwnsBooking(booking, guestId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException(
                    "Payment is only available for confirmed bookings. Current status: " + booking.getStatus());
        }

        var existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.PAID) {
                throw new BusinessRuleException("This booking has already been paid");
            }
            if (payment.getStatus() == PaymentStatus.CREATED && payment.getRazorpayOrderId() != null) {
                return toPaymentResponse(payment);
            }
        }

        try {
            if (demoMode) {
                return createDemoOrder(booking);
            }

            if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
                throw new BusinessRuleException(
                        "Live Razorpay is not configured. Enable demo mode or set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
            }

            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", booking.getTotalAmount());   // already in paise
            options.put("currency", "INR");
            options.put("receipt", booking.getId().toString());

            Order razorpayOrder = razorpayClient.orders.create(options);
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = Payment.builder()
                    .booking(booking)
                    .razorpayOrderId(razorpayOrderId)
                    .amount(booking.getTotalAmount())
                    .currency("INR")
                    .status(PaymentStatus.CREATED)
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Razorpay order created [{}] for booking [{}]", razorpayOrderId, booking.getId());

            return toPaymentResponse(payment);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for booking [{}]: {}", booking.getId(), e.getMessage());
            throw new BusinessRuleException("Failed to create payment order: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Verify payment signature after checkout
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request, UUID guestId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + request.getRazorpayOrderId()));

        assertGuestOwnsBooking(payment.getBooking(), guestId);

        if (isDemoPayment(request)) {
            payment.markPaid(request.getRazorpayPaymentId(), request.getRazorpaySignature());
            payment = paymentRepository.save(payment);
            log.info("Demo payment marked PAID [orderId={}]", request.getRazorpayOrderId());
            return toPaymentResponse(payment);
        }

        if (demoMode) {
            throw new BusinessRuleException("Invalid demo payment credentials");
        }

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            Utils.verifyPaymentSignature(attributes, keySecret);

            // Signature is valid
            payment.markPaid(request.getRazorpayPaymentId(), request.getRazorpaySignature());
            payment = paymentRepository.save(payment);
            log.info("Payment verified and marked PAID [orderId={}]", request.getRazorpayOrderId());

            return toPaymentResponse(payment);
        } catch (RazorpayException e) {
            // Signature mismatch or other verification failure
            log.warn("Payment signature verification failed [orderId={}]: {}",
                    request.getRazorpayOrderId(), e.getMessage());
            payment.markFailed();
            paymentRepository.save(payment);
            throw new BusinessRuleException("Invalid payment signature");
        }
    }

    // -----------------------------------------------------------------------
    // Razorpay webhook handler — must always return 200
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void handleWebhook(String payload, String razorpaySignature) {
        if (demoMode) {
            log.debug("Webhook ignored — payments run in demo mode");
            return;
        }

        try {
            Utils.verifyWebhookSignature(payload, razorpaySignature, keySecret);

            JSONObject event = new JSONObject(payload);
            String eventName = event.optString("event", "");

            if ("payment.captured".equals(eventName)) {
                JSONObject paymentEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String orderId      = paymentEntity.getString("order_id");
                String razorpayPayId = paymentEntity.getString("id");

                paymentRepository.findByRazorpayOrderId(orderId).ifPresentOrElse(
                        p -> {
                            if (p.getStatus() != PaymentStatus.PAID) {
                                p.markPaid(razorpayPayId, null);
                                paymentRepository.save(p);
                                log.info("Webhook: payment [{}] marked PAID via webhook", razorpayPayId);
                            }
                        },
                        () -> log.warn("Webhook: no payment record found for orderId [{}]", orderId)
                );
            } else {
                log.debug("Webhook: unhandled event type [{}]", eventName);
            }
        } catch (RazorpayException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            // Do not rethrow — Razorpay requires HTTP 200 even on our errors
        } catch (Exception e) {
            log.error("Unexpected error while processing webhook: {}", e.getMessage(), e);
            // Same: swallow to return 200
        }
    }

    // -----------------------------------------------------------------------
    // Initiate refund
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public RefundResponse initiateRefund(UUID bookingId, long amount, String reason, UUID adminId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for booking: " + bookingId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException(
                    "Refund can only be initiated for PAID payments. Current status: " + payment.getStatus());
        }

        try {
            if (demoMode) {
                String refundId = "rfnd_demo_" + UUID.randomUUID().toString().replace("-", "");
                Refund refund = Refund.builder()
                        .payment(payment)
                        .booking(payment.getBooking())
                        .razorpayRefundId(refundId)
                        .amount(amount)
                        .reason(reason)
                        .status(RefundStatus.INITIATED)
                        .build();
                refund = refundRepository.save(refund);
                payment.markRefunded();
                paymentRepository.save(payment);
                log.info("Demo refund recorded [{}] for booking [{}]", refundId, bookingId);
                return toRefundResponse(refund);
            }

            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amount);

            com.razorpay.Refund razorpayRefund =
                    razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            String razorpayRefundId = razorpayRefund.get("id");

            Refund refund = Refund.builder()
                    .payment(payment)
                    .booking(payment.getBooking())
                    .razorpayRefundId(razorpayRefundId)
                    .amount(amount)
                    .reason(reason)
                    .status(RefundStatus.INITIATED)
                    .build();

            refund = refundRepository.save(refund);

            // Mark payment as refunded
            payment.markRefunded();
            paymentRepository.save(payment);

            log.info("Refund initiated [{}] for payment [{}] by admin [{}]",
                    razorpayRefundId, payment.getId(), adminId);

            return toRefundResponse(refund);
        } catch (RazorpayException e) {
            log.error("Failed to initiate refund for booking [{}]: {}", bookingId, e.getMessage());
            throw new BusinessRuleException("Failed to initiate refund: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Get payment by booking
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByBooking(UUID bookingId, UUID actorId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getGuest().getId().equals(actorId)
                && !booking.getHost().getId().equals(actorId)) {
            throw new AccessDeniedException("You do not have access to this booking's payment");
        }

        return paymentRepository.findByBookingId(bookingId).map(this::toPaymentResponse);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void assertGuestOwnsBooking(Booking booking, UUID guestId) {
        if (!booking.getGuest().getId().equals(guestId)) {
            throw new AccessDeniedException("You can only pay for your own bookings");
        }
    }

    private PaymentResponse createDemoOrder(Booking booking) {
        String orderId = DEMO_ORDER_PREFIX + UUID.randomUUID().toString().replace("-", "");
        Payment payment = Payment.builder()
                .booking(booking)
                .razorpayOrderId(orderId)
                .amount(booking.getTotalAmount())
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .build();
        payment = paymentRepository.save(payment);
        log.info("Demo payment order created [{}] for booking [{}]", orderId, booking.getId());
        return toPaymentResponse(payment);
    }

    private boolean isDemoPayment(VerifyPaymentRequest request) {
        return demoMode
                && request.getRazorpayOrderId() != null
                && request.getRazorpayOrderId().startsWith(DEMO_ORDER_PREFIX)
                && DEMO_SIGNATURE.equals(request.getRazorpaySignature());
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .bookingId(refund.getBooking().getId())
                .paymentId(refund.getPayment().getId())
                .razorpayRefundId(refund.getRazorpayRefundId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
