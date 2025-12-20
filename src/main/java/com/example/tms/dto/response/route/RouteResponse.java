package com.example.tms.dto.response.route;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.entity.Route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteResponse {
    private UUID id;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private Integer durationDays;
    private String image;
    private Route.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public RouteResponse(Route route) {
        this.id = route.getId();
        this.routeName = route.getRouteName();
        this.startLocation = route.getStartLocation();
        this.endLocation = route.getEndLocation();
        this.durationDays = route.getDurationDays();
        this.image = route.getImage();
        this.status = route.getStatus();
        this.createdAt = route.getCreatedAt();
        this.updatedAt = route.getUpdatedAt();
    }
}

