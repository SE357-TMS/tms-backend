package com.example.tms.controller.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.customer.CreateCustomerBookingRequest;
import com.example.tms.dto.request.customer.UpdatePaymentMethodRequest;
import com.example.tms.dto.request.customer.UpdateTravelerRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.customer.CustomerBookingListResponse;
import com.example.tms.dto.response.customer.CustomerBookingResponse;
import com.example.tms.dto.response.customer.PaymentPageResponse;
import com.example.tms.service.interface_.CustomerBookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/v1/customer/bookings", "/customer/bookings"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
public class CustomerBookingController {
    
    private final CustomerBookingService customerBookingService;
    
    /**
     * Create a new booking
     * POST /api/v1/customer/bookings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> createBooking(
            @Valid @RequestBody CreateCustomerBookingRequest request) {
        CustomerBookingResponse response = customerBookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", response));
    }
    
    /**
     * Get booking by ID
     * GET /api/v1/customer/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> getBookingById(
            @PathVariable UUID bookingId) {
        CustomerBookingResponse response = customerBookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }
    
    /**
     * Get all bookings for current user (reservation list)
     * GET /api/v1/customer/bookings
     * Query params: status (all, paid, unpaid)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerBookingListResponse>>> getMyBookings(
            @RequestParam(required = false, defaultValue = "all") String status) {
        List<CustomerBookingListResponse> response = customerBookingService.getMyBookings(status);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
    }
    
    /**
     * Get payment page data
     * GET /api/v1/customer/bookings/{bookingId}/payment
     */
    @GetMapping("/{bookingId}/payment")
    public ResponseEntity<ApiResponse<PaymentPageResponse>> getPaymentPageData(
            @PathVariable UUID bookingId) {
        PaymentPageResponse response = customerBookingService.getPaymentPageData(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Payment data retrieved successfully", response));
    }
    
    /**
     * Add travelers to a booking
     * POST /api/v1/customer/bookings/{bookingId}/travelers
     */
    @PostMapping("/{bookingId}/travelers")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> addTravelers(
            @PathVariable UUID bookingId,
            @Valid @RequestBody List<UpdateTravelerRequest> travelers) {
        CustomerBookingResponse response = customerBookingService.addTravelers(bookingId, travelers);
        return ResponseEntity.ok(ApiResponse.success("Travelers added successfully", response));
    }
    
    /**
     * Update a specific traveler
     * PUT /api/v1/customer/bookings/{bookingId}/travelers/{travelerId}
     */
    @PutMapping("/{bookingId}/travelers/{travelerId}")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> updateTraveler(
            @PathVariable UUID bookingId,
            @PathVariable UUID travelerId,
            @Valid @RequestBody UpdateTravelerRequest request) {
        CustomerBookingResponse response = customerBookingService.updateTraveler(bookingId, travelerId, request);
        return ResponseEntity.ok(ApiResponse.success("Traveler updated successfully", response));
    }
    
    /**
     * Update booking quantity
     * PUT /api/v1/customer/bookings/{bookingId}/quantity
     */
    @PutMapping("/{bookingId}/quantity")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> updateQuantity(
            @PathVariable UUID bookingId,
            @RequestParam Integer quantity) {
        CustomerBookingResponse response = customerBookingService.updateQuantity(bookingId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Quantity updated successfully", response));
    }
    
    /**
     * Confirm booking
     * POST /api/v1/customer/bookings/{bookingId}/confirm
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> confirmBooking(
            @PathVariable UUID bookingId) {
        CustomerBookingResponse response = customerBookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }
    
    /**
     * Cancel booking
     * POST /api/v1/customer/bookings/{bookingId}/cancel
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> cancelBooking(
            @PathVariable UUID bookingId) {
        CustomerBookingResponse response = customerBookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
    
    /**
     * Update payment method
     * PUT /api/v1/customer/bookings/{bookingId}/payment-method
     */
    @PutMapping("/{bookingId}/payment-method")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> updatePaymentMethod(
            @PathVariable UUID bookingId,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        CustomerBookingResponse response = customerBookingService.updatePaymentMethod(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment method updated successfully", response));
    }
    
    /**
     * Mark booking as paid (for demo/testing)
     * POST /api/v1/customer/bookings/{bookingId}/mark-paid
     */
    @PostMapping("/{bookingId}/mark-paid")
    public ResponseEntity<ApiResponse<CustomerBookingResponse>> markAsPaid(
            @PathVariable UUID bookingId) {
        CustomerBookingResponse response = customerBookingService.markAsPaid(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking marked as paid", response));
    }
}

