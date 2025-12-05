package com.example.tms.dto.request.booking;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CheckoutCartRequest {
    
    @NotEmpty(message = "At least one cart item booking is required")
    @Valid
    private List<CartItemBookingRequest> items;
}
