package com.example.tms.service.interface_;

import java.util.UUID;

import com.example.tms.dto.request.invoice.CreateInvoiceRequest;
import com.example.tms.dto.request.invoice.InvoiceFilterRequest;
import com.example.tms.dto.request.invoice.UpdateInvoiceRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.invoice.InvoiceResponse;

public interface InvoiceService {
    
    InvoiceResponse create(CreateInvoiceRequest request);
    
    InvoiceResponse getById(UUID id);
    
    InvoiceResponse getByBookingId(UUID bookingId);
    
    PaginationResponse<InvoiceResponse> getAll(InvoiceFilterRequest filter);
    
    InvoiceResponse update(UUID id, UpdateInvoiceRequest request);
    
    void delete(UUID id);
    
    InvoiceResponse markAsPaid(UUID id, String paymentMethod);
    
    InvoiceResponse markAsRefunded(UUID id);
}

