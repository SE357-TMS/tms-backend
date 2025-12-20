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
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.Trip;
import com.example.tms.entity.User;

import lombok.Getter;
import lombok.Setter;

/**
 * Full payment page response with contact info, booking details, travelers and invoice
 */
@Getter
@Setter
public class PaymentPageResponse {
    
    // Contact Information (from logged-in user)
    private ContactInfo contactInfo;
    
    // Order Information
    private OrderInfo orderInfo;
    
    @Getter
    @Setter
    public static class ContactInfo {
        private UUID userId;
        private String fullName;
        private String gender; // MALE, FEMALE, OTHER
        private LocalDate dateOfBirth;
        private String email;
        private String phoneNumber;
        private String address;
        private String avatarUrl;
    }
    
    @Getter
    @Setter
    public static class OrderInfo {
        private UUID bookingId;
        private String bookingCode;
        
        // Route info
        private UUID routeId;
        private String routeName;
        private String routeCode;
        private String routeImage;
        private String departureLocation;
        private String destination;
        private Integer durationDays;
        
        // Trip info
        private UUID tripId;
        private LocalDate departureDate;
        private LocalDate returnDate;
        private LocalTime pickUpTime;
        private String pickUpLocation;
        
        // Booking details
        private Integer passengerCount;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private TourBooking.Status bookingStatus;
        
        // Passengers
        private List<PassengerInfo> passengers = new ArrayList<>();
        
        // Invoice
        private InvoiceInfo invoice;
        
        // Timestamps
        private LocalDateTime createdAt;
    }
    
    @Getter
    @Setter
    public static class PassengerInfo {
        private UUID id;
        private String fullName;
        private String gender;
        private LocalDate dateOfBirth;
        private String identityDoc;
        private String email;
        private String phoneNumber;
        private String address;
    }
    
    @Getter
    @Setter
    public static class InvoiceInfo {
        private UUID id;
        private BigDecimal totalAmount;
        private Invoice.PaymentStatus paymentStatus;
        private String paymentMethod;
        private LocalDateTime createdAt;
    }
    
    // Builder methods
    public void setContactFromUser(User user, String avatarUrl) {
        ContactInfo ci = new ContactInfo();
        ci.setUserId(user.getId());
        ci.setFullName(user.getFullName());
        ci.setGender(user.getGender() != null ? mapGender(user.getGender()) : null);
        ci.setDateOfBirth(user.getBirthday());
        ci.setEmail(user.getEmail());
        ci.setPhoneNumber(user.getPhoneNumber());
        ci.setAddress(user.getAddress());
        ci.setAvatarUrl(avatarUrl);
        this.contactInfo = ci;
    }
    
    public void setOrderFromBooking(TourBooking booking, List<BookingTraveler> travelers, 
                                     Invoice invoice, String routeImageUrl) {
        OrderInfo oi = new OrderInfo();
        oi.setBookingId(booking.getId());
        oi.setBookingCode(generateCode("BK", booking.getId()));
        
        Trip trip = booking.getTrip();
        if (trip != null) {
            oi.setTripId(trip.getId());
            oi.setDepartureDate(trip.getDepartureDate());
            oi.setReturnDate(trip.getReturnDate());
            oi.setPickUpTime(trip.getPickUpTime());
            oi.setPickUpLocation(trip.getPickUpLocation());
            oi.setUnitPrice(trip.getPrice());
            
            if (trip.getRoute() != null) {
                var route = trip.getRoute();
                oi.setRouteId(route.getId());
                oi.setRouteName(route.getRouteName());
                oi.setRouteCode(generateCode("", route.getId()));
                oi.setRouteImage(routeImageUrl);
                oi.setDepartureLocation(route.getStartLocation());
                oi.setDestination(route.getEndLocation());
                oi.setDurationDays(route.getDurationDays());
            }
        }
        
        oi.setPassengerCount(booking.getSeatsBooked());
        oi.setTotalPrice(booking.getTotalPrice());
        oi.setBookingStatus(booking.getStatus());
        oi.setCreatedAt(booking.getCreatedAt());
        
        // Set passengers
        List<PassengerInfo> passengerList = new ArrayList<>();
        for (BookingTraveler t : travelers) {
            PassengerInfo pi = new PassengerInfo();
            pi.setId(t.getId());
            pi.setFullName(t.getFullName());
            pi.setGender(t.getGender() != null ? t.getGender().name() : null);
            pi.setDateOfBirth(t.getDateOfBirth());
            pi.setIdentityDoc(t.getIdentityDoc());
            pi.setEmail(t.getEmail());
            pi.setPhoneNumber(t.getPhoneNumber());
            pi.setAddress(t.getAddress());
            passengerList.add(pi);
        }
        oi.setPassengers(passengerList);
        
        // Set invoice
        if (invoice != null) {
            InvoiceInfo ii = new InvoiceInfo();
            ii.setId(invoice.getId());
            ii.setTotalAmount(invoice.getTotalAmount());
            ii.setPaymentStatus(invoice.getPaymentStatus());
            ii.setPaymentMethod(invoice.getPaymentMethod());
            ii.setCreatedAt(invoice.getCreatedAt());
            oi.setInvoice(ii);
        }
        
        this.orderInfo = oi;
    }
    
    private String generateCode(String prefix, UUID id) {
        String idStr = id.toString().replaceAll("-", "").toUpperCase();
        return prefix + idStr.substring(0, 6);
    }
    
    private String mapGender(User.Gender gender) {
        if (gender == null) return null;
        switch (gender) {
            case M: return "MALE";
            case F: return "FEMALE";
            case O: return "OTHER";
            default: return null;
        }
    }
}

