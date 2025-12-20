package com.example.tms.dto.request.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.example.tms.entity.Trip;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTripRequest {
    
    private UUID routeId;
    
    private LocalDate departureDate;
    
    private LocalDate returnDate;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    
    private LocalTime pickUpTime;
    
    @Size(max = 255, message = "Pick up location must be at most 255 characters")
    private String pickUpLocation;
    
    private Trip.Status status;
}

