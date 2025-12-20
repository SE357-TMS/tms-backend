package com.example.tms.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.payment.CreatePaymentRequest;
import com.example.tms.dto.request.payment.VerifyPaymentRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.payment.PaymentInfoResponse;
import com.example.tms.dto.response.payment.PaymentLinkResponse;
import com.example.tms.service.interface_.PayOSService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
public class PaymentController {
    
    private final PayOSService payOSService;
    
    /**
     * Create PayOS payment link
     * POST /api/v1/payment/create-payment
     */
    @PostMapping("/create-payment")
    public ResponseEntity<ApiResponse<PaymentLinkResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment link for booking: {}", request.getBookingId());
        PaymentLinkResponse response = payOSService.createPaymentLink(request);
        return ResponseEntity.ok(ApiResponse.success("Payment link created successfully", response));
    }
    
    /**
     * Get payment info by order code
     * GET /api/v1/payment/payment-requests/{orderCode}
     */
    @GetMapping("/payment-requests/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentInfoResponse>> getPaymentInfo(
            @PathVariable Long orderCode) {
        log.info("Getting payment info for order: {}", orderCode);
        PaymentInfoResponse response = payOSService.getPaymentInfo(orderCode);
        return ResponseEntity.ok(ApiResponse.success("Payment info retrieved successfully", response));
    }
    
    /**
     * Cancel payment link
     * POST /api/v1/payment/payment-requests/{orderCode}/cancel
     */
    @PostMapping("/payment-requests/{orderCode}/cancel")
    public ResponseEntity<ApiResponse<PaymentInfoResponse>> cancelPayment(
            @PathVariable Long orderCode,
            @RequestBody(required = false) Map<String, String> body) {
        String cancellationReason = body != null ? body.get("cancellationReason") : "User cancelled";
        log.info("Cancelling payment for order: {}, reason: {}", orderCode, cancellationReason);
        PaymentInfoResponse response = payOSService.cancelPaymentLink(orderCode, cancellationReason);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", response));
    }
    
    /**
     * Verify payment and update booking status
     * POST /api/v1/payment/verify-payment
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<PaymentInfoResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        log.info("Verifying payment for booking: {}, orderCode: {}", 
                request.getBookingId(), request.getOrderCode());
        PaymentInfoResponse response = payOSService.verifyAndUpdatePayment(
                request.getBookingId(), request.getOrderCode());
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }
    
    /**
     * PayOS Webhook endpoint
     * POST /api/v1/payment/webhook
     */
    @PostMapping("/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Map<String, Object> webhookData) {
        log.info("Received PayOS webhook: {}", webhookData);
        try {
            payOSService.handleWebhook(webhookData);
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "desc", "Webhook received successfully",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Webhook processing error: ", e);
            return ResponseEntity.ok(Map.of(
                "code", "99",
                "desc", e.getMessage(),
                "success", false
            ));
        }
    }
}

