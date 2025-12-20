package com.example.tms.dto.request.tourbooking;

import java.util.UUID;

import com.example.tms.entity.TourBooking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourBookingFilterRequest {
    private UUID userId;
    private UUID tripId;
    private TourBooking.Status status;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}

