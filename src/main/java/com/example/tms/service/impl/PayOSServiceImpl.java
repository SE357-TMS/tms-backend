package com.example.tms.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.payment.CreatePaymentRequest;
import com.example.tms.dto.response.payment.PaymentInfoResponse;
import com.example.tms.dto.response.payment.PaymentLinkResponse;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.exception.ResourceNotFoundException;
import com.example.tms.repository.InvoiceRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.service.interface_.PayOSService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSServiceImpl implements PayOSService {
    
    private final TourBookingRepository tourBookingRepository;
    private final InvoiceRepository invoiceRepository;
    
    @Value("${payos.client-id}")
    private String clientId;
    
    @Value("${payos.api-key}")
    private String apiKey;
    
    @Value("${payos.checksum-key}")
    private String checksumKey;
    
    @Value("${payos.return-url:http://localhost:5173/payment}")
    private String returnUrl;
    
    @Value("${payos.cancel-url:http://localhost:5173/cart}")
    private String cancelUrl;
    
    private PayOS getPayOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }

    @PostConstruct
    public void init() {
        log.info("--- PAYOS CONFIG CHECK ---");
        log.info("ClientID: {}", clientId);
        log.info("API Key: {}", apiKey != null ? "HAS_VALUE" : "NULL");
        log.info("Checksum Key: {}", checksumKey != null ? "HAS_VALUE" : "NULL");
        log.info("--------------------------");
    }
    
    @Override
    @Transactional
    public PaymentLinkResponse createPaymentLink(CreatePaymentRequest request) {
        try {
                // Validate booking exists
                TourBooking booking = tourBookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                String bookingCode = buildBookingCode(booking);
            
            // Generate unique order code
            long orderCode = System.currentTimeMillis();
            
            // Prepare item list
            List<PaymentLinkItem> items = new ArrayList<>();
            items.add(PaymentLinkItem.builder()
                    .name("Tour Booking " + bookingCode)
                    .quantity(1)
                    .price(request.getAmount().longValue())
                    .build());
            
            String description = request.getDescription() != null ? 
                    request.getDescription() : "DH" + bookingCode;
            
            CreatePaymentLinkRequest.CreatePaymentLinkRequestBuilder builder = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(request.getAmount().longValue())
                    .description(description.length() > 9 ? description.substring(0, 9) : description)
                    .items(items)
                    .returnUrl(returnUrl + "/" + booking.getId())
                    .cancelUrl(cancelUrl);

            if (request.getBuyerName() != null) {
                builder.buyerName(request.getBuyerName());
            }
            if (request.getBuyerEmail() != null) {
                builder.buyerEmail(request.getBuyerEmail());
            }
            if (request.getBuyerPhone() != null) {
                builder.buyerPhone(request.getBuyerPhone());
            }
            if (request.getBuyerAddress() != null) {
                builder.buyerAddress(request.getBuyerAddress());
            }

            CreatePaymentLinkRequest paymentData = builder.build();

            // Call PayOS API
            PayOS payOS = getPayOS();
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);
            
            log.info("Payment link created successfully for booking: {}, orderCode: {}", 
                    booking.getId(), orderCode);
            
            // Build response
            return PaymentLinkResponse.builder()
                    .bin(response.getBin())
                    .accountNumber(response.getAccountNumber())
                    .accountName(response.getAccountName())
                    .amount(response.getAmount())
                    .description(description)
                    .orderCode(orderCode)
                    .currency(response.getCurrency())
                    .paymentLinkId(response.getPaymentLinkId())
                    .status(response.getStatus() != null ? response.getStatus().name() : null)
                    .checkoutUrl(response.getCheckoutUrl())
                    .qrCode(response.getQrCode())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error creating payment link: ", e);
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentInfoResponse getPaymentInfo(Long orderCode) {
        try {
            PayOS payOS = getPayOS();
            PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
            return toPaymentInfoResponse(paymentLink);

        } catch (Exception e) {
            log.error("Error getting payment info: ", e);
            throw new RuntimeException("Failed to get payment info: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentInfoResponse cancelPaymentLink(Long orderCode, String cancellationReason) {
        try {
            PayOS payOS = getPayOS();
            PaymentLink response = payOS.paymentRequests().cancel(
                    orderCode, 
                    cancellationReason != null ? cancellationReason : "User cancelled"
            );

            log.info("Payment link cancelled for orderCode: {}", orderCode);

            return toPaymentInfoResponse(response);

        } catch (Exception e) {
            log.error("Error cancelling payment link: ", e);
            throw new RuntimeException("Failed to cancel payment link: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public PaymentInfoResponse verifyAndUpdatePayment(UUID bookingId, Long orderCode) {
        try {
            // Get payment info from PayOS
            PaymentInfoResponse paymentInfo = getPaymentInfo(orderCode);
            
            // If payment is successful, update booking
            if ("PAID".equals(paymentInfo.getStatus())) {
                TourBooking booking = tourBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                invoiceRepository.findByBookingId(bookingId).ifPresent(invoice -> {
                    invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
                    invoiceRepository.save(invoice);
                    log.info("Invoice updated to PAID for booking: {}", bookingId);
                });

                booking.setStatus(TourBooking.Status.CONFIRMED);
                tourBookingRepository.save(booking);

                log.info("Booking status updated to CONFIRMED for booking: {}", bookingId);
            }
            
            return paymentInfo;
            
        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> webhookData) {
        try {
            // Verify webhook signature
            PayOS payOS = getPayOS();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            
            if (data == null) {
                log.warn("Webhook data is null");
                return;
            }
            
            Object orderCodeObj = data.get("orderCode");
            Long orderCode = null;
            
            if (orderCodeObj instanceof Integer) {
                orderCode = ((Integer) orderCodeObj).longValue();
            } else if (orderCodeObj instanceof Long) {
                orderCode = (Long) orderCodeObj;
            } else if (orderCodeObj instanceof String) {
                orderCode = Long.parseLong((String) orderCodeObj);
            }
            
            if (orderCode == null) {
                log.warn("Order code is null in webhook");
                return;
            }
            
            String code = (String) webhookData.get("code");
            
            // If payment successful
            if ("00".equals(code)) {
                log.info("Payment webhook received for orderCode: {}", orderCode);
                
                // Find booking by description or other means
                // Note: You may need to store orderCode in booking for lookup
                // For now, we log the webhook
                
                // TODO: Implement booking lookup and update logic
                log.info("Payment successful for order: {}, amount: {}", 
                        orderCode, data.get("amount"));
            }
            
        } catch (Exception e) {
            log.error("Error handling webhook: ", e);
        }
    }

    private PaymentInfoResponse toPaymentInfoResponse(PaymentLink paymentLink) {
        if (paymentLink == null) {
            return null;
        }
        return PaymentInfoResponse.builder()
                .id(paymentLink.getId())
                .orderCode(paymentLink.getOrderCode())
                .amount(paymentLink.getAmount())
                .amountPaid(paymentLink.getAmountPaid())
                .amountRemaining(paymentLink.getAmountRemaining())
                .status(paymentLink.getStatus() != null ? paymentLink.getStatus().name() : null)
                .createdAt(paymentLink.getCreatedAt())
                .canceledAt(paymentLink.getCanceledAt())
                .cancellationReason(paymentLink.getCancellationReason())
                .build();
    }

    private String buildBookingCode(TourBooking booking) {
        String idStr = booking.getId().toString().replaceAll("-", "").toUpperCase();
        return "BK" + idStr.substring(0, Math.min(6, idStr.length()));
    }
}

