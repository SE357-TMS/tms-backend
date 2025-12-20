package com.example.tms.dto.response.booking;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    private UUID id;
    private UUID bookingId;
    private BigDecimal totalAmount;
    private String paymentStatus; // UNPAID, PAID, REFUNDED
    private String paymentMethod;
}

