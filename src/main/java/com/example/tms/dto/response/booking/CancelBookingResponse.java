package com.example.tms.dto.response.booking;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingResponse {
    
    private String message;
    private Integer daysUntilDeparture;
    private BigDecimal totalPaid;
    private BigDecimal refundAmount;
    private Integer refundPercentage;
    private BigDecimal penaltyAmount;
    private String estimatedRefundTime;
}
