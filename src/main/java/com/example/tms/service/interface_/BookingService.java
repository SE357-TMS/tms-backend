package com.example.tms.service.interface_;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.example.tms.dto.request.booking.BookingFilterRequest;
import com.example.tms.dto.request.booking.CheckoutCartRequest;
import com.example.tms.dto.request.booking.CreateBookingRequest;
import com.example.tms.dto.request.booking.PayInvoiceRequest;
import com.example.tms.dto.request.booking.UpdateTravelersRequest;
import com.example.tms.dto.response.booking.BookingDetailResponse;
import com.example.tms.dto.response.booking.BookingListResponse;
import com.example.tms.dto.response.booking.CancelBookingResponse;
import com.example.tms.dto.response.booking.CheckoutCartResponse;

public interface BookingService {
    
    /**
     * UC_BOOKING_01: Book a Trip directly
     * @param userId Current user ID
     * @param request Booking request with trip info and traveler details
     * @return Created booking details
     */
    BookingDetailResponse createBooking(UUID userId, CreateBookingRequest request);
    
    /**
     * UC_BOOKING_02: Checkout Cart
     * @param userId Current user ID
     * @param request Checkout request with all cart items and traveler details
     * @return Checkout result with all booking IDs
     */
    CheckoutCartResponse checkoutCart(UUID userId, CheckoutCartRequest request);
    
    /**
     * UC_BOOKING_03: View Personal Bookings with filters
     * @param userId Current user ID
     * @param filter Filter criteria
     * @return Paginated list of bookings
     */
    Page<BookingListResponse> getMyBookings(UUID userId, BookingFilterRequest filter);
    
    /**
     * UC_BOOKING_04: View Booking Detail
     * @param userId Current user ID
     * @param bookingId Booking ID to view
     * @return Booking details including invoice and travelers
     */
    BookingDetailResponse getBookingDetail(UUID userId, UUID bookingId);
    
    /**
     * UC_BOOKING_04: Pay Invoice
     * @param userId Current user ID
     * @param bookingId Booking ID
     * @param request Payment method
     * @return Updated booking details
     */
    BookingDetailResponse payInvoice(UUID userId, UUID bookingId, PayInvoiceRequest request);
    
    /**
     * UC_BOOKING_05: Update Travelers Info
     * @param userId Current user ID
     * @param request Update request with traveler details
     * @return Updated booking details
     */
    BookingDetailResponse updateTravelers(UUID userId, UpdateTravelersRequest request);
    
    /**
     * UC_BOOKING_06: Cancel Booking
     * @param userId Current user ID
     * @param bookingId Booking ID to cancel
     * @return Cancel result with refund info
     */
    CancelBookingResponse cancelBooking(UUID userId, UUID bookingId);
    
    /**
     * Get refund preview before canceling
     * @param userId Current user ID
     * @param bookingId Booking ID
     * @return Preview of refund amounts
     */
    CancelBookingResponse previewCancelBooking(UUID userId, UUID bookingId);
}

