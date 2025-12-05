package com.example.tms.dto.request.tourbooking;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.booking.TravelerRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTourBookingRequest {
    
    @NotNull(message = "Trip ID is required")
    private UUID tripId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    private Integer noAdults;
    
    @NotNull(message = "Number of children is required")
    @Min(value = 0, message = "Number of children cannot be negative")
    private Integer noChildren;
    
    @NotEmpty(message = "At least one traveler is required")
    @Valid
    private List<TravelerRequest> travelers;
}
