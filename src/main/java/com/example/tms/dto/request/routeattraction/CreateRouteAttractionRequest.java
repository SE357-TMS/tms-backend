package com.example.tms.dto.request.routeattraction;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRouteAttractionRequest {
    
    @NotNull(message = "Route ID is required")
    private UUID routeId;
    
    @NotNull(message = "Attraction ID is required")
    private UUID attractionId;
    
    @NotNull(message = "Day is required")
    @Min(value = 1, message = "Day must be at least 1")
    private Integer day;
    
    @NotNull(message = "Order in day is required")
    @Min(value = 1, message = "Order in day must be at least 1")
    private Integer orderInDay;
    
    private String activityDescription;
}
