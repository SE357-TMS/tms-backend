package com.example.tms.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.Trip;

import lombok.Getter;
import lombok.Setter;

/**
 * Simplified booking response for reservation list display
 */
@Getter
@Setter
public class CustomerBookingListResponse {
    private UUID id;
    private String bookingCode; // Generated from booking ID
    private String routeCode;   // Generated from route ID
    
    // Basic info for card display
    private String routeName;
    private String routeImage;
    private String departureLocation;
    private String destination;
    
    // Dates
    private LocalDate departureDate;
    private LocalDateTime createdAt;
    
    // Booking details
    private Integer seatsBooked;
    private BigDecimal totalPrice;
    private TourBooking.Status bookingStatus;
    
    // Invoice status (determines card display)
    private Invoice.PaymentStatus invoiceStatus;
    
    // Trip status
    private Trip.Status tripStatus;
    
    public CustomerBookingListResponse(TourBooking booking, Invoice invoice, String routeImageUrl) {
        this.id = booking.getId();
        this.bookingCode = generateCode("BK", booking.getId());
        
        Trip trip = booking.getTrip();
        if (trip != null) {
            this.departureDate = trip.getDepartureDate();
            this.tripStatus = trip.getStatus();
            
            if (trip.getRoute() != null) {
                var route = trip.getRoute();
                this.routeName = route.getRouteName();
                this.routeCode = generateCode("", route.getId());
                this.departureLocation = route.getStartLocation();
                this.destination = route.getEndLocation();
            }
        }
        
        this.routeImage = routeImageUrl;
        this.seatsBooked = booking.getSeatsBooked();
        this.totalPrice = booking.getTotalPrice();
        this.bookingStatus = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        
        if (invoice != null) {
            this.invoiceStatus = invoice.getPaymentStatus();
        } else {
            this.invoiceStatus = Invoice.PaymentStatus.UNPAID;
        }
    }
    
    private String generateCode(String prefix, UUID id) {
        String idStr = id.toString().replaceAll("-", "").toUpperCase();
        return prefix + idStr.substring(0, 6);
    }
    
    /**
     * Determines if this booking card should be displayed as disabled/dimmed
     * Cancelled bookings or expired payments should be dimmed
     */
    public boolean isDisabled() {
        return bookingStatus == TourBooking.Status.CANCELED ||
               invoiceStatus == Invoice.PaymentStatus.REFUNDED ||
               (invoiceStatus == Invoice.PaymentStatus.UNPAID && 
                departureDate != null && departureDate.isBefore(LocalDate.now()));
    }
    
    /**
     * Returns display status text for the button
     */
    public String getDisplayStatus() {
        if (bookingStatus == TourBooking.Status.CANCELED) {
            return "CANCELLED";
        }
        if (invoiceStatus == Invoice.PaymentStatus.PAID) {
            return "PAID";
        }
        if (invoiceStatus == Invoice.PaymentStatus.REFUNDED) {
            return "REFUNDED";
        }
        if (departureDate != null && departureDate.isBefore(LocalDate.now())) {
            return "PAYMENT_OVERDUE";
        }
        return "PAYMENT";
    }
}

