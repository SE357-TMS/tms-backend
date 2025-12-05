package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.trip.CreateTripRequest;
import com.example.tms.dto.request.trip.TripFilterRequest;
import com.example.tms.dto.request.trip.UpdateTripRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.trip.TripResponse;

public interface TripService {
    
    TripResponse create(CreateTripRequest request);
    
    TripResponse getById(UUID id);
    
    PaginationResponse<TripResponse> getAll(TripFilterRequest filter);
    
    List<TripResponse> getAllNoPagination();
    
    TripResponse update(UUID id, UpdateTripRequest request);
    
    void delete(UUID id);
}
