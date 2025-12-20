package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.bookingtraveler.CreateBookingTravelerRequest;
import com.example.tms.dto.request.bookingtraveler.UpdateBookingTravelerRequest;
import com.example.tms.dto.response.bookingtraveler.BookingTravelerResponse;

public interface BookingTravelerService {
    
    BookingTravelerResponse create(CreateBookingTravelerRequest request);
    
    BookingTravelerResponse getById(UUID id);
    
    List<BookingTravelerResponse> getByBookingId(UUID bookingId);
    
    BookingTravelerResponse update(UUID id, UpdateBookingTravelerRequest request);
    
    void delete(UUID id);
}

