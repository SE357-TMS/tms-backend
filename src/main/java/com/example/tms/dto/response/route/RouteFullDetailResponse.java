package com.example.tms.dto.response.route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.example.tms.entity.Route;
import com.example.tms.entity.Trip;

import lombok.Getter;
import lombok.Setter;

/**
 * Combined response for route detail page - includes route info and available trips
 * This avoids additional lookups when rendering route/trip metadata
 */
@Getter
@Setter
public class RouteFullDetailResponse {
    // Route basic info
    private UUID id;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private Integer durationDays;
    private String image;
    private Route.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Itinerary (attractions by day)
    private List<ItineraryDay> itinerary;
    
    // Available trips for booking
    private List<AvailableTrip> availableTrips;
    
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
    
    @Getter
    @Setter
    public static class AvailableTrip {
        private UUID tripId;
        private LocalDate departureDate;
        private LocalDate returnDate;
        private BigDecimal price;
        private Integer totalSeats;
        private Integer bookedSeats;
        private Integer availableSeats;
        private LocalTime pickUpTime;
        private String pickUpLocation;
        private Trip.Status status;
        
        public AvailableTrip(Trip trip) {
            this.tripId = trip.getId();
            this.departureDate = trip.getDepartureDate();
            this.returnDate = trip.getReturnDate();
            this.price = trip.getPrice();
            this.totalSeats = trip.getTotalSeats();
            this.bookedSeats = trip.getBookedSeats();
            this.availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
            this.pickUpTime = trip.getPickUpTime();
            this.pickUpLocation = trip.getPickUpLocation();
            this.status = trip.getStatus();
        }
    }
    
    public RouteFullDetailResponse(Route route) {
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

