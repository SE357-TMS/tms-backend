package com.example.tms.dto.response.cart;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.example.tms.enity.CartItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {
    private UUID id;
    private UUID tripId;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalTime pickUpTime;
    private String pickUpLocation;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    
    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.tripId = cartItem.getTrip().getId();
        if (cartItem.getTrip().getRoute() != null) {
            this.routeName = cartItem.getTrip().getRoute().getRouteName();
            this.startLocation = cartItem.getTrip().getRoute().getStartLocation();
            this.endLocation = cartItem.getTrip().getRoute().getEndLocation();
        }
        this.departureDate = cartItem.getTrip().getDepartureDate();
        this.returnDate = cartItem.getTrip().getReturnDate();
        this.pickUpTime = cartItem.getTrip().getPickUpTime();
        this.pickUpLocation = cartItem.getTrip().getPickUpLocation();
        this.unitPrice = cartItem.getPrice();
        this.quantity = cartItem.getQuantity();
        this.subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        this.availableSeats = cartItem.getTrip().getTotalSeats() - cartItem.getTrip().getBookedSeats();
        this.createdAt = cartItem.getCreatedAt();
    }
}
