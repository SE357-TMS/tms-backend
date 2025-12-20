package com.example.tms.dto.response.route;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.tms.entity.Route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteDetailResponse {
    private UUID id;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private Integer durationDays;
    private String image;
    private Route.Status status;
    private List<String> images;
    private List<ItineraryDay> itinerary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @Setter
    public static class ItineraryDay {
        private Integer day;
        private List<ItineraryAttraction> attractions;
    }
    
    @Getter
    @Setter
    public static class ItineraryAttraction {
        private UUID attractionId;
        private String attractionName;
        private String location;
        private String categoryName;
        private Integer orderInDay;
        private String activityDescription;
    }
    
    public RouteDetailResponse(Route route) {
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

