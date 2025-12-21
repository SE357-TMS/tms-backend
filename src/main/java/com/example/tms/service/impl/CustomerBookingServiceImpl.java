package com.example.tms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.customer.CreateCustomerBookingRequest;
import com.example.tms.dto.request.customer.UpdatePaymentMethodRequest;
import com.example.tms.dto.request.customer.UpdateTravelerRequest;
import com.example.tms.dto.response.customer.CustomerBookingListResponse;
import com.example.tms.dto.response.customer.CustomerBookingResponse;
import com.example.tms.dto.response.customer.PaymentPageResponse;
import com.example.tms.entity.BookingTraveler;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.Trip;
import com.example.tms.entity.User;
import com.example.tms.exception.BadRequestException;
import com.example.tms.exception.ForbiddenException;
import com.example.tms.exception.ResourceNotFoundException;
import com.example.tms.repository.BookingTravelerRepository;
import com.example.tms.repository.CartItemRepository;
import com.example.tms.repository.InvoiceRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.CloudinaryService;
import com.example.tms.service.interface_.CustomerBookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBookingServiceImpl implements CustomerBookingService {

    private final TourBookingRepository bookingRepository;
    private final BookingTravelerRepository travelerRepository;
    private final InvoiceRepository invoiceRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public CustomerBookingResponse createBooking(CreateCustomerBookingRequest request) {
        User currentUser = getCurrentUser();

        // Get trip
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Validate trip
        if (trip.getDeletedAt() != 0) {
            throw new BadRequestException("Trip has been deleted");
        }
        if (trip.getStatus() != Trip.Status.SCHEDULED) {
            throw new BadRequestException("Trip is not available for booking");
        }
        if (trip.getDepartureDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book a trip that has already departed");
        }

        if (request.getCartItemId() != null) {
            bookingRepository.findActiveByCartItemId(request.getCartItemId())
                    .ifPresent(existing -> {
                        throw new BadRequestException("A temporary booking already exists for this cart item");
                    });
        }

        // Check available seats
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        if (request.getQuantity() > availableSeats) {
            throw new BadRequestException("Not enough available seats. Available: " + availableSeats);
        }

        // Create booking
        TourBooking booking = new TourBooking();
        booking.setTrip(trip);
        booking.setUser(currentUser);
        booking.setSeatsBooked(request.getQuantity());
        booking.setTotalPrice(trip.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        booking.setCartItemId(request.getCartItemId());
        booking.setStatus(TourBooking.Status.PENDING);

        booking = bookingRepository.save(booking);

        // Update trip booked seats
        trip.setBookedSeats(trip.getBookedSeats() + request.getQuantity());
        tripRepository.save(trip);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setTourBooking(booking);
        invoice.setTotalAmount(booking.getTotalPrice());
        invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        invoiceRepository.save(invoice);

        // Add travelers if provided
        List<BookingTraveler> travelers = new ArrayList<>();
        if (request.getTravelers() != null && !request.getTravelers().isEmpty()) {
            for (CreateCustomerBookingRequest.TravelerInfo ti : request.getTravelers()) {
                BookingTraveler traveler = new BookingTraveler();
                traveler.setTourBooking(booking);
                traveler.setFullName(ti.getFullName());
                traveler.setGender(parseGender(ti.getGender()));
                traveler.setDateOfBirth(safeParseTravelDate(ti.getDateOfBirth()));
                traveler.setIdentityDoc(ti.getIdentityDoc());
                traveler.setEmail(ti.getEmail());
                traveler.setPhoneNumber(ti.getPhoneNumber());
                traveler.setAddress(ti.getAddress());
                travelers.add(travelerRepository.save(traveler));
            }
        }

        // Build response
        String routeImage = getRouteImage(trip);
        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    public CustomerBookingResponse getBookingById(UUID bookingId) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getDeletedAt() != 0) {
            throw new ResourceNotFoundException("Booking has been deleted");
        }

        // Verify ownership
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to view this booking");
        }

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);

        String routeImage = getRouteImage(booking.getTrip());
        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    public List<CustomerBookingListResponse> getMyBookings(String statusFilter) {
        User currentUser = getCurrentUser();

        List<TourBooking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        List<CustomerBookingListResponse> responses = new ArrayList<>();
        for (TourBooking booking : bookings) {
            if (booking.getDeletedAt() != 0)
                continue;

            // Apply filter if provided
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("all")) {
                Invoice invoice = invoiceRepository.findByBookingId(booking.getId()).orElse(null);

                if (statusFilter.equalsIgnoreCase("paid")) {
                    if (invoice == null || invoice.getPaymentStatus() != Invoice.PaymentStatus.PAID) {
                        continue;
                    }
                } else if (statusFilter.equalsIgnoreCase("unpaid")) {
                    if (invoice != null && invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
                        continue;
                    }
                }
            }

            Invoice invoice = invoiceRepository.findByBookingId(booking.getId()).orElse(null);
            String routeImage = getRouteImage(booking.getTrip());

            responses.add(new CustomerBookingListResponse(booking, invoice, routeImage));
        }

        return responses;
    }

    @Override
    public PaymentPageResponse getPaymentPageData(UUID bookingId) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getDeletedAt() != 0) {
            throw new ResourceNotFoundException("Booking has been deleted");
        }

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to view this booking");
        }

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);

        String userAvatarUrl = cloudinaryService.getUserAvatarUrl(currentUser.getId());
        String routeImage = getRouteImage(booking.getTrip());

        PaymentPageResponse response = new PaymentPageResponse();
        response.setContactFromUser(currentUser, userAvatarUrl);
        response.setOrderFromBooking(booking, travelers, invoice, routeImage);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse addTravelers(UUID bookingId, List<UpdateTravelerRequest> travelers) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        if (booking.getStatus() != TourBooking.Status.PENDING) {
            throw new BadRequestException("Cannot add travelers to a non-pending booking");
        }

        // Get current traveler count
        List<BookingTraveler> existingTravelers = travelerRepository.findByBookingId(bookingId);
        int currentCount = existingTravelers.size();
        int newCount = currentCount + travelers.size();

        if (newCount > booking.getSeatsBooked()) {
            throw new BadRequestException("Total travelers (" + newCount +
                    ") exceeds booked seats (" + booking.getSeatsBooked() + ")");
        }

        List<BookingTraveler> allTravelers = new ArrayList<>(existingTravelers);

        for (UpdateTravelerRequest tr : travelers) {
            BookingTraveler traveler = new BookingTraveler();
            traveler.setTourBooking(booking);
            traveler.setFullName(tr.getFullName());
            traveler.setGender(tr.getGender());
            traveler.setDateOfBirth(tr.getDateOfBirth());
            traveler.setIdentityDoc(tr.getIdentityDoc());
            traveler.setEmail(tr.getEmail());
            traveler.setPhoneNumber(tr.getPhoneNumber());
            traveler.setAddress(tr.getAddress());
            allTravelers.add(travelerRepository.save(traveler));
        }

        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        String routeImage = getRouteImage(booking.getTrip());

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(allTravelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse updateTraveler(UUID bookingId, UUID travelerId, UpdateTravelerRequest request) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        BookingTraveler traveler = travelerRepository.findByIdAndBookingId(travelerId, bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Traveler not found"));

        traveler.setFullName(request.getFullName());
        traveler.setGender(request.getGender());
        traveler.setDateOfBirth(request.getDateOfBirth());
        traveler.setIdentityDoc(request.getIdentityDoc());
        traveler.setEmail(request.getEmail());
        traveler.setPhoneNumber(request.getPhoneNumber());
        traveler.setAddress(request.getAddress());

        travelerRepository.save(traveler);

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        String routeImage = getRouteImage(booking.getTrip());

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse updateQuantity(UUID bookingId, Integer newQuantity) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        if (booking.getStatus() != TourBooking.Status.PENDING) {
            throw new BadRequestException("Cannot modify a non-pending booking");
        }

        Trip trip = booking.getTrip();
        int currentBooked = booking.getSeatsBooked();
        int difference = newQuantity - currentBooked;

        // Check if increasing
        if (difference > 0) {
            int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
            if (difference > availableSeats) {
                throw new BadRequestException("Not enough available seats. Available: " + availableSeats);
            }
        }

        // Check if travelers exceed new quantity
        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        if (travelers.size() > newQuantity) {
            throw new BadRequestException(
                    "Cannot reduce quantity below number of travelers (" + travelers.size() + ")");
        }

        // Update booking
        booking.setSeatsBooked(newQuantity);
        booking.setTotalPrice(trip.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        bookingRepository.save(booking);

        // Update trip booked seats
        trip.setBookedSeats(trip.getBookedSeats() + difference);
        tripRepository.save(trip);

        // Update invoice
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        if (invoice != null) {
            invoice.setTotalAmount(booking.getTotalPrice());
            invoiceRepository.save(invoice);
        }

        String routeImage = getRouteImage(trip);
        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse confirmBooking(UUID bookingId) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new BadRequestException("Booking is already canceled");
        }
        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new BadRequestException("Booking is already completed");
        }

        // Verify traveler count
        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        if (travelers.size() < booking.getSeatsBooked()) {
            throw new BadRequestException("Please add all passenger information. Required: " +
                    booking.getSeatsBooked() + ", Added: " + travelers.size());
        }

        // This method is called when customer clicks "Book Now" to proceed to payment
        // Status will be updated to CONFIRMED after payment is completed (via
        // markAsPaid or PayOS webhook)
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        String routeImage = getRouteImage(booking.getTrip());

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse cancelBooking(UUID bookingId) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new BadRequestException("Booking is already canceled");
        }

        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed booking");
        }

        // Return seats to trip
        Trip trip = booking.getTrip();
        trip.setBookedSeats(trip.getBookedSeats() - booking.getSeatsBooked());
        tripRepository.save(trip);

        // Update booking status
        booking.setStatus(TourBooking.Status.CANCELED);
        bookingRepository.save(booking);

        // Update invoice if exists
        Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        if (invoice != null && invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            invoice.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);
            invoiceRepository.save(invoice);
        }

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        String routeImage = getRouteImage(trip);

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse updatePaymentMethod(UUID bookingId, UpdatePaymentMethodRequest request) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        invoice.setPaymentMethod(request.getPaymentMethod());
        invoiceRepository.save(invoice);

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        String routeImage = getRouteImage(booking.getTrip());

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public CustomerBookingResponse markAsPaid(UUID bookingId) {
        User currentUser = getCurrentUser();

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to modify this booking");
        }

        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        invoiceRepository.save(invoice);

        // Also confirm booking if pending
        if (booking.getStatus() == TourBooking.Status.PENDING) {
            booking.setStatus(TourBooking.Status.CONFIRMED);
            bookingRepository.save(booking);
        }

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(bookingId);
        String routeImage = getRouteImage(booking.getTrip());

        CustomerBookingResponse response = new CustomerBookingResponse(booking, routeImage);
        response.setTravelersFromEntities(travelers);
        response.setInvoiceFromEntity(invoice);

        return response;
    }

    @Override
    @Transactional
    public void deleteBookingByCartItem(UUID cartItemId) {
        if (cartItemId == null) {
            return;
        }

        User currentUser = getCurrentUser();
        bookingRepository.findActiveByCartItemId(cartItemId).ifPresent(booking -> {
            if (!booking.getUser().getId().equals(currentUser.getId())) {
                return;
            }
            if (booking.getStatus() != TourBooking.Status.PENDING) {
                return;
            }

            cleanupBookingResources(booking);
            booking.markAsDeleted();
            bookingRepository.save(booking);
        });
    }

    private void cleanupBookingResources(TourBooking booking) {
        if (booking == null) {
            return;
        }

        Trip trip = booking.getTrip();
        if (trip != null) {
            int updatedSeats = Math.max(0, trip.getBookedSeats() - booking.getSeatsBooked());
            trip.setBookedSeats(updatedSeats);
            tripRepository.save(trip);
        }

        List<BookingTraveler> travelers = travelerRepository.findByBookingId(booking.getId());
        travelers.forEach(BookingTraveler::markAsDeleted);
        if (!travelers.isEmpty()) {
            travelerRepository.saveAll(travelers);
        }

        invoiceRepository.findByBookingId(booking.getId()).ifPresent(invoice -> {
            invoice.markAsDeleted();
            invoiceRepository.save(invoice);
        });
    }

    private void removeCartItemForBooking(TourBooking booking) {
        if (booking.getCartItemId() == null) {
            return;
        }
        cartItemRepository.findById(booking.getCartItemId()).ifPresent(cartItem -> {
            cartItem.setDeletedAt(System.currentTimeMillis());
            cartItemRepository.save(cartItem);
        });
    }

    // Helper methods
    private String getRouteImage(Trip trip) {
        if (trip == null || trip.getRoute() == null) {
            return null;
        }
        String routeImage = trip.getRoute().getImage();
        if (routeImage != null && !routeImage.isBlank()) {
            return routeImage;
        }
        List<String> images = cloudinaryService.getRouteImages(trip.getRoute().getId());
        return images.isEmpty() ? null : images.get(0);
    }

    private LocalDate safeParseTravelDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid traveler date format: " + isoDate);
        }
    }

    private BookingTraveler.Gender parseGender(String gender) {
        if (gender == null)
            return null;
        try {
            return BookingTraveler.Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
