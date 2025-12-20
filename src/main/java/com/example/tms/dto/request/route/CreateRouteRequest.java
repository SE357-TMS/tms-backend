package com.example.tms.dto.request.route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRouteRequest {
    
    @NotBlank(message = "Route name is required")
    @Size(max = 100, message = "Route name must be at most 100 characters")
    private String routeName;
    
    @NotBlank(message = "Start location is required")
    @Size(max = 100, message = "Start location must be at most 100 characters")
    private String startLocation;
    
    @NotBlank(message = "End location is required")
    @Size(max = 100, message = "End location must be at most 100 characters")
    private String endLocation;
    
    private Integer durationDays;
    
    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String image;
}

