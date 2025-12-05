package com.example.tms.dto.request.invoice;

import java.util.UUID;

import com.example.tms.enity.Invoice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceFilterRequest {
    private UUID bookingId;
    private UUID userId;
    private Invoice.PaymentStatus paymentStatus;
    private String paymentMethod;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
