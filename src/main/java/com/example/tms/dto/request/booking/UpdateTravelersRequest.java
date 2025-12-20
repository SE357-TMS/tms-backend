package com.example.tms.dto.request.booking;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTravelersRequest {
    
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    @NotEmpty(message = "At least one traveler is required")
    @Valid
    private List<UpdateTravelerRequest> travelers;
}

