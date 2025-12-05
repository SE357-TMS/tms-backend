package com.example.tms.dto.request.trip;

import java.time.LocalDate;
import java.util.UUID;

import com.example.tms.enity.Trip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripFilterRequest {
    private UUID routeId;
    private LocalDate departureDateFrom;
    private LocalDate departureDateTo;
    private LocalDate returnDateFrom;
    private LocalDate returnDateTo;
    private Trip.Status status;
    private Boolean hasAvailableSeats;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "departureDate";
    private String sortDirection = "asc";
}
