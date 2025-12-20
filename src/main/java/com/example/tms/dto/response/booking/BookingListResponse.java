package com.example.tms.dto.response.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListResponse {
    
    private UUID id;
    private String routeName;
    private String routeImage;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer seatsBooked;
    private BigDecimal totalPrice;
    private String bookingStatus; // PENDING, CONFIRMED, CANCELED, COMPLETED
    private String paymentStatus; // UNPAID, PAID, REFUNDED
    private LocalDateTime createdAt;
}

