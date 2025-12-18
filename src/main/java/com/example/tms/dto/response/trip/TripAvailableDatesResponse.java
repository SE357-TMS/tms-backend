package com.example.tms.dto.response.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.example.tms.enity.Trip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripAvailableDatesResponse {
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
    
    public TripAvailableDatesResponse(Trip trip) {
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
