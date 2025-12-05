package com.example.tms.dto.response.tourbooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.tms.enity.TourBooking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourBookingResponse {
    private UUID id;
    private UUID tripId;
    private String routeName;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private UUID userId;
    private String userName;
    private String userEmail;
    private Integer seatsBooked;
    private BigDecimal totalPrice;
    private TourBooking.Status status;
    private Integer noAdults;
    private Integer noChildren;
    private List<TravelerInfoResponse> travelers;
    private InvoiceInfoResponse invoice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public TourBookingResponse(TourBooking booking) {
        this.id = booking.getId();
        if (booking.getTrip() != null) {
            this.tripId = booking.getTrip().getId();
            this.departureDate = booking.getTrip().getDepartureDate();
            this.returnDate = booking.getTrip().getReturnDate();
            if (booking.getTrip().getRoute() != null) {
                this.routeName = booking.getTrip().getRoute().getRouteName();
            }
        }
        if (booking.getUser() != null) {
            this.userId = booking.getUser().getId();
            this.userName = booking.getUser().getFullName();
            this.userEmail = booking.getUser().getEmail();
        }
        this.seatsBooked = booking.getSeatsBooked();
        this.totalPrice = booking.getTotalPrice();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
    }
    
    @Getter
    @Setter
    public static class TravelerInfoResponse {
        private UUID id;
        private String fullName;
        private String gender;
        private LocalDate dateOfBirth;
        private String identityDoc;
    }
    
    @Getter
    @Setter
    public static class InvoiceInfoResponse {
        private UUID id;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String paymentMethod;
    }
}
