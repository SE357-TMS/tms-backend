package com.example.tms.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoResponse {
    private String id;
    private Long orderCode;
    private Long amount;
    private Long amountPaid;
    private Long amountRemaining;
    private String status;
    private String createdAt;
    private String canceledAt;
    private String cancellationReason;
}

