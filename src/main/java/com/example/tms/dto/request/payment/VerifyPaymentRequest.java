package com.example.tms.dto.request.payment;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
    
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    @NotNull(message = "Order code is required")
    private Long orderCode;
}

