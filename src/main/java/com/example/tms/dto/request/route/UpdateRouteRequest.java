package com.example.tms.dto.request.route;

import com.example.tms.entity.Route;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRouteRequest {
    
    @Size(max = 100, message = "Route name must be at most 100 characters")
    private String routeName;
    
    @Size(max = 100, message = "Start location must be at most 100 characters")
    private String startLocation;
    
    @Size(max = 100, message = "End location must be at most 100 characters")
    private String endLocation;
    
    private Integer durationDays;
    
    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String image;
    
    private Route.Status status;
}

