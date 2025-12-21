package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.tourbooking.CreateTourBookingRequest;
import com.example.tms.dto.request.tourbooking.TourBookingFilterRequest;
import com.example.tms.dto.request.tourbooking.UpdateTourBookingRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.tourbooking.TourBookingResponse;

public interface TourBookingService {

    TourBookingResponse create(CreateTourBookingRequest request);

    TourBookingResponse getById(UUID id);

    PaginationResponse<TourBookingResponse> getAll(TourBookingFilterRequest filter);

    List<TourBookingResponse> getByUserId(UUID userId);

    TourBookingResponse update(UUID id, UpdateTourBookingRequest request);

    void delete(UUID id);

    void cancel(UUID id);

    void removeTraveler(UUID bookingId, UUID travelerId);
}
