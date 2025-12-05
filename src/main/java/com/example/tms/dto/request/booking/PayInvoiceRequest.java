package com.example.tms.dto.request.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayInvoiceRequest {
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., "Chuyển khoản ngân hàng", "Thẻ tín dụng", "Ví điện tử MoMo", "Ví điện tử ZaloPay"
}
