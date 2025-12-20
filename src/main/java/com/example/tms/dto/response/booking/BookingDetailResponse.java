package com.example.tms.dto.response.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    
    private UUID id;
    private UUID tripId;
    private UUID userId;
    
    // Trip info
    private String routeName;
    private String startLocation;
    private String endLocation;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BigDecimal pricePerPerson;
    private LocalTime pickUpTime;
    private String pickUpLocation;
    private String tripStatus;
    
    // Booking info
    private Integer seatsBooked;
    private Integer noAdults;
    private Integer noChildren;
    private BigDecimal totalPrice;
    private String bookingStatus; // PENDING, CONFIRMED, CANCELED, COMPLETED
    private LocalDateTime createdAt;
    
    // Travelers
    private List<TravelerResponse> travelers;
    
    // Invoice
    private InvoiceResponse invoice;
    
    // Permissions
    private Boolean canEdit; // Can edit traveler info
    private Boolean canCancel; // Can cancel booking
    private Boolean canPay; // Can pay invoice
}

