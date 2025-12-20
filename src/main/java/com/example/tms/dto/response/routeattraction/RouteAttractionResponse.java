package com.example.tms.dto.response.routeattraction;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.entity.RouteAttraction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteAttractionResponse {
    private UUID id;
    private UUID routeId;
    private String routeName;
    private UUID attractionId;
    private String attractionName;
    private Integer day;
    private Integer orderInDay;
    private String activityDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public RouteAttractionResponse(RouteAttraction routeAttraction) {
        this.id = routeAttraction.getId();
        if (routeAttraction.getRoute() != null) {
            this.routeId = routeAttraction.getRoute().getId();
            this.routeName = routeAttraction.getRoute().getRouteName();
        }
        if (routeAttraction.getAttraction() != null) {
            this.attractionId = routeAttraction.getAttraction().getId();
            this.attractionName = routeAttraction.getAttraction().getName();
        }
        this.day = routeAttraction.getDay();
        this.orderInDay = routeAttraction.getOrderInDay();
        this.activityDescription = routeAttraction.getActivityDescription();
        this.createdAt = routeAttraction.getCreatedAt();
        this.updatedAt = routeAttraction.getUpdatedAt();
    }
}

