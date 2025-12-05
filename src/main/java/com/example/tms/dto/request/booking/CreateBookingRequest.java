package com.example.tms.dto.request.booking;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    
    @NotNull(message = "Trip ID is required")
    private UUID tripId;
    
    @NotNull(message = "Number of adults is required")
    @Min(value = 0, message = "Number of adults must be at least 0")
    private Integer noAdults;
    
    @NotNull(message = "Number of children is required")
    @Min(value = 0, message = "Number of children must be at least 0")
    private Integer noChildren;
    
    @NotEmpty(message = "At least one traveler is required")
    @Valid
    private List<TravelerRequest> travelers;
}
