package com.example.tms.dto.request.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTripRequest {
    
    @NotNull(message = "Route ID is required")
    private UUID routeId;
    
    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;
    
    @NotNull(message = "Return date is required")
    private LocalDate returnDate;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    
    private LocalTime pickUpTime;
    
    @Size(max = 255, message = "Pick up location must be at most 255 characters")
    private String pickUpLocation;
}

