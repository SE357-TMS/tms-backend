package com.example.tms.dto.request.payment;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1000, message = "Minimum amount is 1000 VND")
    private Integer amount;
    
    private String description;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerAddress;
}

