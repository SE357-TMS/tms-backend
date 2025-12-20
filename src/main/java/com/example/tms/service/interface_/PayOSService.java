package com.example.tms.service.interface_;

import java.util.Map;
import java.util.UUID;

import com.example.tms.dto.request.payment.CreatePaymentRequest;
import com.example.tms.dto.response.payment.PaymentInfoResponse;
import com.example.tms.dto.response.payment.PaymentLinkResponse;

public interface PayOSService {
    
    /**
     * Create PayOS payment link
     * @param request Payment creation request
     * @return Payment link response with QR code and checkout URL
     */
    PaymentLinkResponse createPaymentLink(CreatePaymentRequest request);
    
    /**
     * Get payment information by order code
     * @param orderCode PayOS order code
     * @return Payment info response
     */
    PaymentInfoResponse getPaymentInfo(Long orderCode);
    
    /**
     * Cancel payment link
     * @param orderCode PayOS order code
     * @param cancellationReason Reason for cancellation
     * @return Payment info response
     */
    PaymentInfoResponse cancelPaymentLink(Long orderCode, String cancellationReason);
    
    /**
     * Verify payment status and update booking if paid
     * @param bookingId Booking ID to update
     * @param orderCode PayOS order code
     * @return Payment info response
     */
    PaymentInfoResponse verifyAndUpdatePayment(UUID bookingId, Long orderCode);
    
    /**
     * Handle webhook from PayOS
     * @param webhookData Webhook payload
     */
    void handleWebhook(Map<String, Object> webhookData);
}

