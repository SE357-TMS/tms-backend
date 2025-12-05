package com.example.tms.dto.request.booking;

import java.time.LocalDate;

import lombok.Data;

@Data
public class BookingFilterRequest {
    
    private String bookingStatus; // PENDING, CONFIRMED, CANCELED, COMPLETED
    private String paymentStatus; // UNPAID, PAID, REFUNDED
    private LocalDate fromDate;
    private LocalDate toDate;
    private String routeName;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    
    // Sorting
    private String sortBy = "departureDate";
    private String sortDirection = "DESC";
}
