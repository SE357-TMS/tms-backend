package com.example.tms.dto.request.routeattraction;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRouteAttractionRequest {
    
    private UUID routeId;
    
    private UUID attractionId;
    
    @Min(value = 1, message = "Day must be at least 1")
    private Integer day;
    
    @Min(value = 1, message = "Order in day must be at least 1")
    private Integer orderInDay;
    
    private String activityDescription;
}

