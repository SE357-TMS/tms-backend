package com.example.tms.dto.response.cart;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.example.tms.entity.CartItem;
import com.example.tms.entity.Route;
import com.example.tms.entity.Trip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {
    private UUID id;
    private UUID tripId;
    private UUID routeId;
    private String routeName;
    private String routeCode;
    private String startLocation;
    private String endLocation;
    private Integer durationDays;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalTime pickUpTime;
    private String pickUpLocation;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    private String routeImage;
    private String routeStatus;
    private String tripStatus;
    private Boolean isExpired;
    private UUID pendingBookingId;
    private String pendingBookingStatus;
    
    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.tripId = cartItem.getTrip().getId();
        
        Trip trip = cartItem.getTrip();
        Route route = trip.getRoute();
        
        if (route != null) {
            this.routeId = route.getId();
            this.routeName = route.getRouteName();
            this.routeCode = generateRouteCode(route.getId());
            this.startLocation = route.getStartLocation();
            this.endLocation = route.getEndLocation();
            this.durationDays = route.getDurationDays();
            this.routeImage = route.getImage();
            this.routeStatus = route.getStatus() != null ? route.getStatus().name() : null;
        }
        
        this.departureDate = trip.getDepartureDate();
        this.returnDate = trip.getReturnDate();
        this.pickUpTime = trip.getPickUpTime();
        this.pickUpLocation = trip.getPickUpLocation();
        this.unitPrice = cartItem.getPrice();
        this.quantity = cartItem.getQuantity();
        this.subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        this.availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        this.createdAt = cartItem.getCreatedAt();
        this.tripStatus = trip.getStatus() != null ? trip.getStatus().name() : null;
        
        // Check if trip is expired (departure date is in the past)
        this.isExpired = trip.getDepartureDate() != null && trip.getDepartureDate().isBefore(LocalDate.now());
    }
    
    private String generateRouteCode(UUID routeId) {
        if (routeId == null) return "";
        String raw = routeId.toString().replace("-", "").toUpperCase();
        return raw.substring(0, Math.min(8, raw.length()));
    }
}

