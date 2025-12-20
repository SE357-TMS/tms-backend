package com.example.tms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.booking.BookingFilterRequest;
import com.example.tms.dto.request.booking.CartItemBookingRequest;
import com.example.tms.dto.request.booking.CheckoutCartRequest;
import com.example.tms.dto.request.booking.CreateBookingRequest;
import com.example.tms.dto.request.booking.PayInvoiceRequest;
import com.example.tms.dto.request.booking.TravelerRequest;
import com.example.tms.dto.request.booking.UpdateTravelerRequest;
import com.example.tms.dto.request.booking.UpdateTravelersRequest;
import com.example.tms.dto.response.booking.BookingDetailResponse;
import com.example.tms.dto.response.booking.BookingListResponse;
import com.example.tms.dto.response.booking.CancelBookingResponse;
import com.example.tms.dto.response.booking.CheckoutCartResponse;
import com.example.tms.dto.response.booking.InvoiceResponse;
import com.example.tms.dto.response.booking.TravelerResponse;
import com.example.tms.entity.BookingTraveler;
import com.example.tms.entity.Cart;
import com.example.tms.entity.CartItem;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.TourBookingDetail;
import com.example.tms.entity.Trip;
import com.example.tms.entity.User;
import com.example.tms.repository.BookingTravelerRepository;
import com.example.tms.repository.CartItemRepository;
import com.example.tms.repository.CartRepository;
import com.example.tms.repository.InvoiceRepository;
import com.example.tms.repository.TourBookingDetailRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.BookingService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    
    private final TourBookingRepository tourBookingRepository;
    private final TourBookingDetailRepository tourBookingDetailRepository;
    private final BookingTravelerRepository bookingTravelerRepository;
    private final InvoiceRepository invoiceRepository;
    private final TripRepository tripRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    
    // Minimum days before departure to book
    private static final int MIN_DAYS_TO_BOOK = 2;
    // Minimum days before departure to edit travelers
    private static final int MIN_DAYS_TO_EDIT = 3;
    
    // Refund policy thresholds
    private static final int REFUND_80_DAYS = 15;
    private static final int REFUND_50_DAYS = 7;
    private static final int REFUND_20_DAYS = 3;
    
    @Override
    @Transactional
    public BookingDetailResponse createBooking(UUID userId, CreateBookingRequest request) {
        log.info("Creating booking for user: {} on trip: {}", userId, request.getTripId());
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get trip with lock to prevent race condition
        Trip trip = tripRepository.findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        
        // Validate trip status
        validateTripForBooking(trip);
        
        // Validate number of travelers
        int totalTravelers = request.getNoAdults() + request.getNoChildren();
        if (totalTravelers <= 0) {
            throw new RuntimeException("Number of travelers must be greater than 0");
        }
        
        if (request.getTravelers().size() != totalTravelers) {
            throw new RuntimeException("Number of traveler details must match total travelers count");
        }
        
        // Check available seats
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        if (availableSeats < totalTravelers) {
            throw new RuntimeException("Not enough seats available. Only " + availableSeats + " seats remaining");
        }
        
        // Calculate total price
        BigDecimal totalPrice = trip.getPrice().multiply(BigDecimal.valueOf(totalTravelers));
        
        // Create booking
        TourBooking booking = new TourBooking();
        booking.setTrip(trip);
        booking.setUser(user);
        booking.setSeatsBooked(totalTravelers);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(TourBooking.Status.PENDING);
        booking = tourBookingRepository.save(booking);
        
        // Create booking detail
        TourBookingDetail detail = new TourBookingDetail();
        detail.setTourBooking(booking);
        detail.setNoAdults(request.getNoAdults());
        detail.setNoChildren(request.getNoChildren());
        tourBookingDetailRepository.save(detail);
        
        // Create travelers
        for (TravelerRequest travelerReq : request.getTravelers()) {
            BookingTraveler traveler = new BookingTraveler();
            traveler.setTourBooking(booking);
            traveler.setFullName(travelerReq.getFullName());
            traveler.setGender(BookingTraveler.Gender.valueOf(travelerReq.getGender()));
            traveler.setDateOfBirth(travelerReq.getDateOfBirth());
            traveler.setIdentityDoc(travelerReq.getIdentityDoc());
            bookingTravelerRepository.save(traveler);
        }
        
        // Update trip booked seats
        trip.setBookedSeats(trip.getBookedSeats() + totalTravelers);
        tripRepository.save(trip);
        
        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setTourBooking(booking);
        invoice.setTotalAmount(totalPrice);
        invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        invoiceRepository.save(invoice);
        
        log.info("Booking created successfully with ID: {}", booking.getId());
        
        // TODO: Send confirmation email
        
        return getBookingDetail(userId, booking.getId());
    }
    
    @Override
    @Transactional
    public CheckoutCartResponse checkoutCart(UUID userId, CheckoutCartRequest request) {
        log.info("Checking out cart for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        List<UUID> bookingIds = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CartItemBookingRequest itemRequest : request.getItems()) {
            // Get cart item
            CartItem cartItem = cartItemRepository.findByIdAndCartId(itemRequest.getCartItemId(), cart.getId())
                    .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemRequest.getCartItemId()));
            
            // Get trip with lock
            Trip trip = tripRepository.findByIdWithLock(cartItem.getTrip().getId())
                    .orElseThrow(() -> new RuntimeException("Trip not found"));
            
            // Validate trip
            validateTripForBooking(trip);
            
            // Validate travelers
            int totalTravelers = itemRequest.getNoAdults() + itemRequest.getNoChildren();
            if (totalTravelers <= 0) {
                throw new RuntimeException("Number of travelers must be greater than 0 for cart item: " + itemRequest.getCartItemId());
            }
            
            if (itemRequest.getTravelers().size() != totalTravelers) {
                throw new RuntimeException("Number of traveler details must match total travelers count for cart item: " + itemRequest.getCartItemId());
            }
            
            // Check available seats
            int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
            if (availableSeats < totalTravelers) {
                throw new RuntimeException("Not enough seats for trip: " + trip.getRoute().getRouteName() + ". Only " + availableSeats + " seats remaining");
            }
            
            // Calculate price
            BigDecimal bookingPrice = trip.getPrice().multiply(BigDecimal.valueOf(totalTravelers));
            totalAmount = totalAmount.add(bookingPrice);
            
            // Create booking
            TourBooking booking = new TourBooking();
            booking.setTrip(trip);
            booking.setUser(user);
            booking.setSeatsBooked(totalTravelers);
            booking.setTotalPrice(bookingPrice);
            booking.setStatus(TourBooking.Status.PENDING);
            booking = tourBookingRepository.save(booking);
            bookingIds.add(booking.getId());
            
            // Create booking detail
            TourBookingDetail detail = new TourBookingDetail();
            detail.setTourBooking(booking);
            detail.setNoAdults(itemRequest.getNoAdults());
            detail.setNoChildren(itemRequest.getNoChildren());
            tourBookingDetailRepository.save(detail);
            
            // Create travelers
            for (TravelerRequest travelerReq : itemRequest.getTravelers()) {
                BookingTraveler traveler = new BookingTraveler();
                traveler.setTourBooking(booking);
                traveler.setFullName(travelerReq.getFullName());
                traveler.setGender(BookingTraveler.Gender.valueOf(travelerReq.getGender()));
                traveler.setDateOfBirth(travelerReq.getDateOfBirth());
                traveler.setIdentityDoc(travelerReq.getIdentityDoc());
                bookingTravelerRepository.save(traveler);
            }
            
            // Update trip booked seats
            trip.setBookedSeats(trip.getBookedSeats() + totalTravelers);
            tripRepository.save(trip);
            
            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setTourBooking(booking);
            invoice.setTotalAmount(bookingPrice);
            invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
            invoiceRepository.save(invoice);
        }
        
        // Soft delete all cart items
        cartItemRepository.softDeleteByCartId(cart.getId(), System.currentTimeMillis());
        
        log.info("Cart checkout completed. {} bookings created", bookingIds.size());
        
        // TODO: Send confirmation emails
        
        return CheckoutCartResponse.builder()
                .bookingIds(bookingIds)
                .totalBookings(bookingIds.size())
                .totalAmount(totalAmount)
                .message("Checkout successful. " + bookingIds.size() + " booking(s) created.")
                .build();
    }
    
    @Override
    public Page<BookingListResponse> getMyBookings(UUID userId, BookingFilterRequest filter) {
        log.info("Getting bookings for user: {} with filter: {}", userId, filter);
        
        // Build sort
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                mapSortField(filter.getSortBy())
        );
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        // Build specification
        Specification<TourBooking> spec = buildBookingSpecification(userId, filter);
        
        Page<TourBooking> bookings = tourBookingRepository.findAll(spec, pageable);
        
        return bookings.map(this::toBookingListResponse);
    }
    
    @Override
    public BookingDetailResponse getBookingDetail(UUID userId, UUID bookingId) {
        log.info("Getting booking detail: {} for user: {}", bookingId, userId);
        
        TourBooking booking = tourBookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you don't have access"));
        
        return toBookingDetailResponse(booking);
    }
    
    @Override
    @Transactional
    public BookingDetailResponse payInvoice(UUID userId, UUID bookingId, PayInvoiceRequest request) {
        log.info("Paying invoice for booking: {} by user: {}", bookingId, userId);
        
        TourBooking booking = tourBookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you don't have access"));
        
        // Validate booking status
        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new RuntimeException("Cannot pay for a canceled booking");
        }
        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new RuntimeException("Booking is already completed");
        }
        
        // Get invoice
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Validate invoice status
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            throw new RuntimeException("Invoice is already paid");
        }
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.REFUNDED) {
            throw new RuntimeException("Invoice has been refunded");
        }
        
        // Validate trip hasn't started
        Trip trip = booking.getTrip();
        if (trip.getDepartureDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot pay for a trip that has already departed");
        }
        
        // TODO: Process actual payment through payment gateway
        
        // Update invoice
        invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoiceRepository.save(invoice);
        
        // Update booking status to CONFIRMED
        booking.setStatus(TourBooking.Status.CONFIRMED);
        tourBookingRepository.save(booking);
        
        log.info("Payment successful for booking: {}", bookingId);
        
        // TODO: Send payment confirmation email with e-ticket
        
        return getBookingDetail(userId, bookingId);
    }
    
    @Override
    @Transactional
    public BookingDetailResponse updateTravelers(UUID userId, UpdateTravelersRequest request) {
        log.info("Updating travelers for booking: {} by user: {}", request.getBookingId(), userId);
        
        TourBooking booking = tourBookingRepository.findByIdAndUserId(request.getBookingId(), userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you don't have access"));
        
        // Validate booking status
        if (booking.getStatus() != TourBooking.Status.PENDING && 
            booking.getStatus() != TourBooking.Status.CONFIRMED) {
            throw new RuntimeException("Cannot edit travelers for a " + booking.getStatus() + " booking");
        }
        
        // Validate days until departure
        Trip trip = booking.getTrip();
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), trip.getDepartureDate());
        if (daysUntilDeparture < MIN_DAYS_TO_EDIT) {
            throw new RuntimeException("Cannot edit travelers within " + MIN_DAYS_TO_EDIT + " days of departure. Please contact support.");
        }
        
        // Update each traveler
        for (UpdateTravelerRequest travelerReq : request.getTravelers()) {
            BookingTraveler traveler = bookingTravelerRepository.findByIdAndBookingId(
                    travelerReq.getTravelerId(), request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Traveler not found: " + travelerReq.getTravelerId()));
            
            traveler.setFullName(travelerReq.getFullName());
            traveler.setGender(BookingTraveler.Gender.valueOf(travelerReq.getGender()));
            traveler.setDateOfBirth(travelerReq.getDateOfBirth());
            traveler.setIdentityDoc(travelerReq.getIdentityDoc());
            bookingTravelerRepository.save(traveler);
        }
        
        log.info("Travelers updated successfully for booking: {}", request.getBookingId());
        
        // TODO: Send update confirmation email
        
        return getBookingDetail(userId, request.getBookingId());
    }
    
    @Override
    @Transactional
    public CancelBookingResponse cancelBooking(UUID userId, UUID bookingId) {
        log.info("Canceling booking: {} by user: {}", bookingId, userId);
        
        TourBooking booking = tourBookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you don't have access"));
        
        // Validate booking can be canceled
        validateBookingCanBeCanceled(booking);
        
        Trip trip = booking.getTrip();
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), trip.getDepartureDate());
        
        // Get invoice
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Calculate refund
        int refundPercentage = calculateRefundPercentage(daysUntilDeparture);
        BigDecimal totalPaid = invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID ? 
                invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = totalPaid.multiply(BigDecimal.valueOf(refundPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal penaltyAmount = totalPaid.subtract(refundAmount);
        
        // Update booking status
        booking.setStatus(TourBooking.Status.CANCELED);
        tourBookingRepository.save(booking);
        
        // Update invoice if paid
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            invoice.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);
            invoiceRepository.save(invoice);
            // TODO: Create refund request record
        }
        
        // Release seats
        trip.setBookedSeats(trip.getBookedSeats() - booking.getSeatsBooked());
        tripRepository.save(trip);
        
        log.info("Booking {} canceled successfully", bookingId);
        
        // TODO: Send cancellation email
        
        return CancelBookingResponse.builder()
                .message("Booking canceled successfully")
                .daysUntilDeparture((int) daysUntilDeparture)
                .totalPaid(totalPaid)
                .refundAmount(refundAmount)
                .refundPercentage(refundPercentage)
                .penaltyAmount(penaltyAmount)
                .estimatedRefundTime(refundAmount.compareTo(BigDecimal.ZERO) > 0 ? "7-10 business days" : null)
                .build();
    }
    
    @Override
    public CancelBookingResponse previewCancelBooking(UUID userId, UUID bookingId) {
        log.info("Preview cancel booking: {} for user: {}", bookingId, userId);
        
        TourBooking booking = tourBookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you don't have access"));
        
        // Validate booking can be canceled
        validateBookingCanBeCanceled(booking);
        
        Trip trip = booking.getTrip();
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), trip.getDepartureDate());
        
        // Get invoice
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Calculate refund preview
        int refundPercentage = calculateRefundPercentage(daysUntilDeparture);
        BigDecimal totalPaid = invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID ? 
                invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = totalPaid.multiply(BigDecimal.valueOf(refundPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal penaltyAmount = totalPaid.subtract(refundAmount);
        
        return CancelBookingResponse.builder()
                .message("Cancel booking preview")
                .daysUntilDeparture((int) daysUntilDeparture)
                .totalPaid(totalPaid)
                .refundAmount(refundAmount)
                .refundPercentage(refundPercentage)
                .penaltyAmount(penaltyAmount)
                .estimatedRefundTime(refundAmount.compareTo(BigDecimal.ZERO) > 0 ? "7-10 business days" : null)
                .build();
    }
    
    // ============ Helper Methods ============
    
    private void validateTripForBooking(Trip trip) {
        // Check trip status
        if (trip.getStatus() != Trip.Status.SCHEDULED) {
            throw new RuntimeException("Trip is not available for booking. Status: " + trip.getStatus());
        }
        
        // Check departure date (must be at least MIN_DAYS_TO_BOOK days in future)
        LocalDate minDepartureDate = LocalDate.now().plusDays(MIN_DAYS_TO_BOOK);
        if (trip.getDepartureDate().isBefore(minDepartureDate)) {
            throw new RuntimeException("Trip departure date must be at least " + MIN_DAYS_TO_BOOK + " days from now");
        }
    }
    
    private void validateBookingCanBeCanceled(TourBooking booking) {
        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new RuntimeException("Booking is already canceled");
        }
        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }
        
        Trip trip = booking.getTrip();
        if (trip.getDepartureDate().isBefore(LocalDate.now()) || 
            trip.getDepartureDate().isEqual(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel a booking for a trip that has already started");
        }
    }
    
    private int calculateRefundPercentage(long daysUntilDeparture) {
        if (daysUntilDeparture >= REFUND_80_DAYS) {
            return 80;
        } else if (daysUntilDeparture >= REFUND_50_DAYS) {
            return 50;
        } else if (daysUntilDeparture >= REFUND_20_DAYS) {
            return 20;
        } else {
            return 0;
        }
    }
    
    private String mapSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "departuredate" -> "trip.departureDate";
            case "createdat" -> "createdAt";
            case "totalprice" -> "totalPrice";
            case "status" -> "status";
            default -> "trip.departureDate";
        };
    }
    
    private Specification<TourBooking> buildBookingSpecification(UUID userId, BookingFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // User filter
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            
            // Not deleted
            predicates.add(cb.equal(root.get("deletedAt"), 0L));
            
            // Status filter
            if (filter.getBookingStatus() != null && !filter.getBookingStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), 
                        TourBooking.Status.valueOf(filter.getBookingStatus())));
            }
            
            // Date range filter
            Join<TourBooking, Trip> tripJoin = root.join("trip", JoinType.INNER);
            
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(tripJoin.get("departureDate"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(tripJoin.get("departureDate"), filter.getToDate()));
            }
            
            // Route name filter
            if (filter.getRouteName() != null && !filter.getRouteName().isEmpty()) {
                Join<Trip, ?> routeJoin = tripJoin.join("route", JoinType.INNER);
                predicates.add(cb.like(cb.lower(routeJoin.get("routeName")), 
                        "%" + filter.getRouteName().toLowerCase() + "%"));
            }
            
            // Payment status filter
            if (filter.getPaymentStatus() != null && !filter.getPaymentStatus().isEmpty()) {
                // This requires a subquery or join to Invoice
                // For simplicity, we'll skip this in the specification and filter in memory
                // In production, use a proper subquery
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    private BookingListResponse toBookingListResponse(TourBooking booking) {
        Trip trip = booking.getTrip();
        
        // Get invoice for payment status
        Invoice invoice = invoiceRepository.findByBookingId(booking.getId()).orElse(null);
        
        return BookingListResponse.builder()
                .id(booking.getId())
                .routeName(trip.getRoute().getRouteName())
                .routeImage(trip.getRoute().getImage())
                .departureDate(trip.getDepartureDate())
                .returnDate(trip.getReturnDate())
                .seatsBooked(booking.getSeatsBooked())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getStatus().name())
                .paymentStatus(invoice != null ? invoice.getPaymentStatus().name() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }
    
    private BookingDetailResponse toBookingDetailResponse(TourBooking booking) {
        Trip trip = booking.getTrip();
        
        // Get booking detail
        TourBookingDetail detail = tourBookingDetailRepository.findByBookingId(booking.getId())
                .orElse(null);
        
        // Get travelers
        List<BookingTraveler> travelers = bookingTravelerRepository.findByBookingId(booking.getId());
        List<TravelerResponse> travelerResponses = travelers.stream()
                .map(this::toTravelerResponse)
                .collect(Collectors.toList());
        
        // Get invoice
        Invoice invoice = invoiceRepository.findByBookingId(booking.getId()).orElse(null);
        InvoiceResponse invoiceResponse = invoice != null ? toInvoiceResponse(invoice) : null;
        
        // Calculate permissions
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), trip.getDepartureDate());
        boolean canEdit = (booking.getStatus() == TourBooking.Status.PENDING || 
                          booking.getStatus() == TourBooking.Status.CONFIRMED) &&
                         daysUntilDeparture >= MIN_DAYS_TO_EDIT;
        boolean canCancel = (booking.getStatus() == TourBooking.Status.PENDING || 
                            booking.getStatus() == TourBooking.Status.CONFIRMED) &&
                           daysUntilDeparture > 0;
        boolean canPay = invoice != null && 
                        invoice.getPaymentStatus() == Invoice.PaymentStatus.UNPAID &&
                        booking.getStatus() != TourBooking.Status.CANCELED &&
                        daysUntilDeparture > 0;
        
        return BookingDetailResponse.builder()
                .id(booking.getId())
                .tripId(trip.getId())
                .userId(booking.getUser().getId())
                .routeName(trip.getRoute().getRouteName())
                .startLocation(trip.getRoute().getStartLocation())
                .endLocation(trip.getRoute().getEndLocation())
                .departureDate(trip.getDepartureDate())
                .returnDate(trip.getReturnDate())
                .pricePerPerson(trip.getPrice())
                .pickUpTime(trip.getPickUpTime())
                .pickUpLocation(trip.getPickUpLocation())
                .tripStatus(trip.getStatus().name())
                .seatsBooked(booking.getSeatsBooked())
                .noAdults(detail != null ? detail.getNoAdults() : null)
                .noChildren(detail != null ? detail.getNoChildren() : null)
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .travelers(travelerResponses)
                .invoice(invoiceResponse)
                .canEdit(canEdit)
                .canCancel(canCancel)
                .canPay(canPay)
                .build();
    }
    
    private TravelerResponse toTravelerResponse(BookingTraveler traveler) {
        return TravelerResponse.builder()
                .id(traveler.getId())
                .fullName(traveler.getFullName())
                .gender(traveler.getGender() != null ? traveler.getGender().name() : null)
                .dateOfBirth(traveler.getDateOfBirth())
                .identityDoc(traveler.getIdentityDoc())
                .build();
    }
    
    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .bookingId(invoice.getTourBooking().getId())
                .totalAmount(invoice.getTotalAmount())
                .paymentStatus(invoice.getPaymentStatus().name())
                .paymentMethod(invoice.getPaymentMethod())
                .build();
    }
}

