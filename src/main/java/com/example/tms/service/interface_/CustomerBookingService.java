package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.customer.CreateCustomerBookingRequest;
import com.example.tms.dto.request.customer.UpdatePaymentMethodRequest;
import com.example.tms.dto.request.customer.UpdateTravelerRequest;
import com.example.tms.dto.response.customer.CustomerBookingListResponse;
import com.example.tms.dto.response.customer.CustomerBookingResponse;
import com.example.tms.dto.response.customer.PaymentPageResponse;

public interface CustomerBookingService {
    
    /**
     * Create a new booking for the current user
     * If coming from cart, removes the cart item after successful booking
     */
    CustomerBookingResponse createBooking(CreateCustomerBookingRequest request);
    
    /**
     * Get booking by ID for current user
     */
    CustomerBookingResponse getBookingById(UUID bookingId);
    
    /**
     * Get all bookings for current user (for reservation list)
     */
    List<CustomerBookingListResponse> getMyBookings(String statusFilter);
    
    /**
     * Get payment page data for a specific booking
     */
    PaymentPageResponse getPaymentPageData(UUID bookingId);
    
    /**
     * Add travelers to a booking
     */
    CustomerBookingResponse addTravelers(UUID bookingId, List<UpdateTravelerRequest> travelers);
    
    /**
     * Update a specific traveler
     */
    CustomerBookingResponse updateTraveler(UUID bookingId, UUID travelerId, UpdateTravelerRequest request);
    
    /**
     * Update quantity (seats) for a booking - also updates total price
     */
    CustomerBookingResponse updateQuantity(UUID bookingId, Integer newQuantity);
    
    /**
     * Confirm booking (change status from PENDING to CONFIRMED)
     * Only allowed when traveler count matches seats booked
     */
    CustomerBookingResponse confirmBooking(UUID bookingId);
    
    /**
     * Cancel booking (change status to CANCELED)
     */
    CustomerBookingResponse cancelBooking(UUID bookingId);
    
    /**
     * Update payment method for booking's invoice
     */
    CustomerBookingResponse updatePaymentMethod(UUID bookingId, UpdatePaymentMethodRequest request);
    
    /**
     * Mark invoice as paid (for testing/demo purposes)
     */
    CustomerBookingResponse markAsPaid(UUID bookingId);

    /**
     * Remove any temporary booking that was created from a cart item
     */
    void deleteBookingByCartItem(UUID cartItemId);
}

