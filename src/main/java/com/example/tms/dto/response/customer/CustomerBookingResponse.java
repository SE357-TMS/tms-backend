package com.example.tms.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.tms.entity.BookingTraveler;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.Route;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.Trip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerBookingResponse {
    private UUID id;
    private String bookingCode; // Generated from booking ID
    
    // Trip Information
    private UUID tripId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalTime pickUpTime;
    private String pickUpLocation;
    private Trip.Status tripStatus;
    
    // Route Information
    private UUID routeId;
    private String routeName;
    private String routeCode;
    private String routeDescription;
    private String departureLocation;
    private String destination;
    private Integer durationDays;
    private String routeImage;
    
    // Booking details
    private Integer seatsBooked;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private TourBooking.Status status;
    
    // Travelers
    private List<TravelerResponse> travelers = new ArrayList<>();
    private Integer travelerCount;
    
    // Invoice
    private InvoiceResponse invoice;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CustomerBookingResponse(TourBooking booking, String routeImageUrl) {
        this.id = booking.getId();
        this.bookingCode = generateBookingCode(booking.getId());
        
        Trip trip = booking.getTrip();
        if (trip != null) {
            this.tripId = trip.getId();
            this.departureDate = trip.getDepartureDate();
            this.returnDate = trip.getReturnDate();
            this.pickUpTime = trip.getPickUpTime();
            this.pickUpLocation = trip.getPickUpLocation();
            this.tripStatus = trip.getStatus();
            this.unitPrice = trip.getPrice();
            
            Route route = trip.getRoute();
            if (route != null) {
                this.routeId = route.getId();
                this.routeName = route.getRouteName();
                this.routeCode = generateRouteCode(route.getId());
                this.routeDescription = buildRouteDescription(route);
                this.departureLocation = route.getStartLocation();
                this.destination = route.getEndLocation();
                this.durationDays = route.getDurationDays();
                this.routeImage = routeImageUrl;
            }
        }
        
        this.seatsBooked = booking.getSeatsBooked();
        this.totalPrice = booking.getTotalPrice();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
    }
    
    public void setTravelersFromEntities(List<BookingTraveler> travelerEntities) {
        this.travelers = new ArrayList<>();
        for (BookingTraveler t : travelerEntities) {
            TravelerResponse tr = new TravelerResponse();
            tr.setId(t.getId());
            tr.setFullName(t.getFullName());
            tr.setGender(t.getGender());
            tr.setDateOfBirth(t.getDateOfBirth());
            tr.setIdentityDoc(t.getIdentityDoc());
            tr.setEmail(t.getEmail());
            tr.setPhoneNumber(t.getPhoneNumber());
            tr.setAddress(t.getAddress());
            this.travelers.add(tr);
        }
        this.travelerCount = this.travelers.size();
    }
    
    public void setInvoiceFromEntity(Invoice invoiceEntity) {
        if (invoiceEntity != null) {
            InvoiceResponse ir = new InvoiceResponse();
            ir.setId(invoiceEntity.getId());
            ir.setTotalAmount(invoiceEntity.getTotalAmount());
            ir.setPaymentStatus(invoiceEntity.getPaymentStatus());
            ir.setPaymentMethod(invoiceEntity.getPaymentMethod());
            ir.setCreatedAt(invoiceEntity.getCreatedAt());
            this.invoice = ir;
        }
    }
    
    private String generateBookingCode(UUID id) {
        String idStr = id.toString().replaceAll("-", "").toUpperCase();
        return "BK" + idStr.substring(0, 6);
    }
    
    private String generateRouteCode(UUID id) {
        String idStr = id.toString().replaceAll("-", "").toUpperCase();
        return idStr.substring(0, 6);
    }

    private String buildRouteDescription(Route route) {
        String start = route.getStartLocation();
        String end = route.getEndLocation();
        if (start != null && end != null) {
            return start + " - " + end;
        }
        if (start != null) {
            return start;
        }
        if (end != null) {
            return end;
        }
        return route.getRouteName();
    }
    
    @Getter
    @Setter
    public static class TravelerResponse {
        private UUID id;
        private String fullName;
        private BookingTraveler.Gender gender;
        private LocalDate dateOfBirth;
        private String identityDoc;
        private String email;
        private String phoneNumber;
        private String address;
    }
    
    @Getter
    @Setter
    public static class InvoiceResponse {
        private UUID id;
        private BigDecimal totalAmount;
        private Invoice.PaymentStatus paymentStatus;
        private String paymentMethod;
        private LocalDateTime createdAt;
    }
}

