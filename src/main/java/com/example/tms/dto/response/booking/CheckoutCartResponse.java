package com.example.tms.dto.response.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutCartResponse {
    
    private List<UUID> bookingIds;
    private Integer totalBookings;
    private BigDecimal totalAmount;
    private String message;
}
