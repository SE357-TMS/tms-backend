package com.example.tms.dto.request.tourbooking;

import java.time.LocalDate;
import java.util.UUID;

import com.example.tms.enity.TourBooking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourBookingFilterRequest {
    private UUID userId;
    private UUID tripId;
    private TourBooking.Status status;

    // Search by keyword (customer name, email, route name)
    private String keyword;

    // Filter by booking date range
    private LocalDate fromDate;
    private LocalDate toDate;

    // Filter by departure date range
    private LocalDate departureFrom;
    private LocalDate departureTo;

    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;

    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
