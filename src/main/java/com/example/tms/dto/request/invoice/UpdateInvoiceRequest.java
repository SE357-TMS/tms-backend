package com.example.tms.dto.request.invoice;

import java.math.BigDecimal;

import com.example.tms.enity.Invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInvoiceRequest {
    
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;
    
    private Invoice.PaymentStatus paymentStatus;
    
    @Size(max = 50, message = "Payment method must be at most 50 characters")
    private String paymentMethod;
}
