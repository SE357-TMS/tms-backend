package com.example.tms.service.interface_;

import java.util.UUID;

import com.example.tms.dto.request.bookingdetail.CreateBookingDetailRequest;
import com.example.tms.dto.request.bookingdetail.UpdateBookingDetailRequest;
import com.example.tms.dto.response.bookingdetail.BookingDetailResponse;

public interface BookingDetailService {
    
    BookingDetailResponse create(CreateBookingDetailRequest request);
    
    BookingDetailResponse getById(UUID id);
    
    BookingDetailResponse getByBookingId(UUID bookingId);
    
    BookingDetailResponse update(UUID id, UpdateBookingDetailRequest request);
    
    void delete(UUID id);
}
