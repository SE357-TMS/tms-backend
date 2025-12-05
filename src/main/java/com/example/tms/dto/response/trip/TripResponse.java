package com.example.tms.dto.response.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.example.tms.enity.Trip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripResponse {
    private UUID id;
    private UUID routeId;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BigDecimal price;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Integer availableSeats;
    private LocalTime pickUpTime;
    private String pickUpLocation;
    private Trip.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public TripResponse(Trip trip) {
        this.id = trip.getId();
        if (trip.getRoute() != null) {
            this.routeId = trip.getRoute().getId();
            this.routeName = trip.getRoute().getRouteName();
            this.startLocation = trip.getRoute().getStartLocation();
            this.endLocation = trip.getRoute().getEndLocation();
        }
        this.departureDate = trip.getDepartureDate();
        this.returnDate = trip.getReturnDate();
        this.price = trip.getPrice();
        this.totalSeats = trip.getTotalSeats();
        this.bookedSeats = trip.getBookedSeats();
        this.availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        this.pickUpTime = trip.getPickUpTime();
        this.pickUpLocation = trip.getPickUpLocation();
        this.status = trip.getStatus();
        this.createdAt = trip.getCreatedAt();
        this.updatedAt = trip.getUpdatedAt();
    }
}
