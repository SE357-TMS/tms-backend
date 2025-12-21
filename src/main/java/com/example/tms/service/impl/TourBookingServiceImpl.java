package com.example.tms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.example.tms.dto.request.booking.TravelerRequest;
import com.example.tms.dto.request.tourbooking.CreateTourBookingRequest;
import com.example.tms.dto.request.tourbooking.TourBookingFilterRequest;
import com.example.tms.dto.request.tourbooking.UpdateTourBookingRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.tourbooking.TourBookingResponse;
import com.example.tms.entity.BookingTraveler;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.TourBookingDetail;
import com.example.tms.entity.Trip;
import com.example.tms.entity.User;
import com.example.tms.repository.BookingTravelerRepository;
import com.example.tms.repository.InvoiceRepository;
import com.example.tms.repository.TourBookingDetailRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.TourBookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourBookingServiceImpl implements TourBookingService {

    private final TourBookingRepository tourBookingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TourBookingDetailRepository tourBookingDetailRepository;
    private final BookingTravelerRepository bookingTravelerRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public TourBookingResponse create(CreateTourBookingRequest request) {
        // Validate trip
        Trip trip = tripRepository.findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != Trip.Status.SCHEDULED) {
            throw new RuntimeException("Trip is not available for booking");
        }

        // Validate departure date
        if (trip.getDepartureDate().isBefore(LocalDate.now().plusDays(2))) {
            throw new RuntimeException("Cannot book a trip less than 2 days before departure");
        }

        // Validate user
        User user = userRepository.findById(request.getUserId())
                .filter(u -> u.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate total travelers
        int totalTravelers = request.getNoAdults() + request.getNoChildren();

        // Validate travelers list
        if (request.getTravelers().size() != totalTravelers) {
            throw new RuntimeException("Number of travelers does not match the sum of adults and children");
        }

        // Check seat availability
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        if (totalTravelers > availableSeats) {
            throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
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

        TourBooking savedBooking = tourBookingRepository.save(booking);

        // Update trip booked seats
        trip.setBookedSeats(trip.getBookedSeats() + totalTravelers);
        tripRepository.save(trip);

        // Create booking detail
        TourBookingDetail detail = new TourBookingDetail();
        detail.setTourBooking(savedBooking);
        detail.setNoAdults(request.getNoAdults());
        detail.setNoChildren(request.getNoChildren());
        tourBookingDetailRepository.save(detail);

        // Create travelers
        for (TravelerRequest travelerReq : request.getTravelers()) {
            BookingTraveler traveler = new BookingTraveler();
            traveler.setTourBooking(savedBooking);
            traveler.setFullName(travelerReq.getFullName());
            if (travelerReq.getGender() != null) {
                traveler.setGender(BookingTraveler.Gender.valueOf(travelerReq.getGender()));
            }
            traveler.setDateOfBirth(travelerReq.getDateOfBirth());
            traveler.setIdentityDoc(travelerReq.getIdentityDoc());
            traveler.setEmail(travelerReq.getEmail());
            traveler.setPhoneNumber(travelerReq.getPhoneNumber());
            bookingTravelerRepository.save(traveler);
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setTourBooking(savedBooking);
        invoice.setTotalAmount(totalPrice);
        invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        invoiceRepository.save(invoice);

        return buildResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public TourBookingResponse getById(UUID id) {
        TourBooking booking = tourBookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return buildResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<TourBookingResponse> getAll(TourBookingFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<TourBooking> spec = buildSpecification(filter);
        Page<TourBooking> page = tourBookingRepository.findAll(spec, pageable);

        List<TourBookingResponse> items = page.getContent().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourBookingResponse> getByUserId(UUID userId) {
        return tourBookingRepository.findAll().stream()
                .filter(b -> b.getDeletedAt() == 0)
                .filter(b -> b.getUser().getId().equals(userId))
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TourBookingResponse update(UUID id, UpdateTourBookingRequest request) {
        TourBooking booking = tourBookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Update status if provided
        if (request.getStatus() != null) {
            if (request.getStatus() == TourBooking.Status.CONFIRMED) {
                Invoice existingInvoice = invoiceRepository.findByBookingId(id).orElse(null);
                if (existingInvoice == null || existingInvoice.getPaymentStatus() != Invoice.PaymentStatus.PAID) {
                    throw new RuntimeException("Cannot confirm booking before payment is completed");
                }
            }
            booking.setStatus(request.getStatus());
        }

        // Update travelers if provided (only before departure)
        if (request.getTravelers() != null && !request.getTravelers().isEmpty()) {
            // IMPORTANT: Lock Trip to prevent race condition when changing seat count
            Trip trip = tripRepository.findByIdWithLock(booking.getTrip().getId())
                    .orElseThrow(() -> new RuntimeException("Trip not found"));

            // Check invoice status for payment restrictions
            Invoice invoice = invoiceRepository.findByBookingId(id).orElse(null);
            boolean isUnpaid = invoice == null || invoice.getPaymentStatus() == Invoice.PaymentStatus.UNPAID;
            boolean isPaid = invoice != null && invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID;

            // Editing travelers is allowed only when:
            // - invoice is UNPAID, OR
            // - time to departure is greater than 3 days
            boolean isMoreThan3DaysToDeparture = trip.getDepartureDate() != null
                    && trip.getDepartureDate().isAfter(LocalDate.now().plusDays(3));
            if (!isUnpaid && !isMoreThan3DaysToDeparture) {
                throw new RuntimeException(
                        "Cannot edit travelers after payment when departure is within 3 days");
            }

            // Check if trip has not departed yet
            if (trip.getDepartureDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Cannot edit travelers after departure date");
            }

            // Check if booking is still editable
            if (booking.getStatus() == TourBooking.Status.CANCELED ||
                    booking.getStatus() == TourBooking.Status.COMPLETED) {
                throw new RuntimeException("Cannot edit canceled or completed booking");
            }

            int newTotalTravelers = request.getTravelers().size();
            int currentTravelers = booking.getSeatsBooked();

            // If payment already completed, allow only traveler info updates (no
            // add/remove)
            if (isPaid && newTotalTravelers != currentTravelers) {
                throw new RuntimeException("Cannot add or remove travelers after payment is completed");
            }

            // If number of travelers changed (only when Unpaid), check seat availability
            if (newTotalTravelers != currentTravelers) {
                int seatDifference = newTotalTravelers - currentTravelers;

                // If adding travelers, check availability
                if (seatDifference > 0) {
                    int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
                    if (seatDifference > availableSeats) {
                        throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
                    }
                }

                // Update trip booked seats
                trip.setBookedSeats(trip.getBookedSeats() + seatDifference);
                tripRepository.save(trip);

                // Update booking seats and price
                booking.setSeatsBooked(newTotalTravelers);
                BigDecimal newPrice = trip.getPrice().multiply(BigDecimal.valueOf(newTotalTravelers));
                booking.setTotalPrice(newPrice);

                // Update invoice if exists
                if (invoice != null) {
                    invoice.setTotalAmount(newPrice);
                    invoiceRepository.save(invoice);
                }
            }

            // Delete old travelers (soft delete)
            List<BookingTraveler> oldTravelers = bookingTravelerRepository.findByBookingId(id);
            for (BookingTraveler traveler : oldTravelers) {
                traveler.markAsDeleted();
                bookingTravelerRepository.save(traveler);
            }

            // Create new travelers
            for (TravelerRequest travelerReq : request.getTravelers()) {
                BookingTraveler traveler = new BookingTraveler();
                traveler.setTourBooking(booking);
                traveler.setFullName(travelerReq.getFullName());
                if (travelerReq.getGender() != null) {
                    traveler.setGender(BookingTraveler.Gender.valueOf(travelerReq.getGender()));
                }
                traveler.setDateOfBirth(travelerReq.getDateOfBirth());
                traveler.setIdentityDoc(travelerReq.getIdentityDoc());
                traveler.setEmail(travelerReq.getEmail());
                traveler.setPhoneNumber(travelerReq.getPhoneNumber());
                bookingTravelerRepository.save(traveler);
            }

            // Update booking detail
            tourBookingDetailRepository.findByBookingId(id).ifPresent(detail -> {
                int noAdults = request.getNoAdults() != null ? request.getNoAdults()
                        : (int) request.getTravelers().stream()
                                .filter(t -> t.getDateOfBirth() == null ||
                                        t.getDateOfBirth().isBefore(LocalDate.now().minusYears(12)))
                                .count();
                int noChildren = newTotalTravelers - noAdults;

                detail.setNoAdults(noAdults);
                detail.setNoChildren(noChildren);
                tourBookingDetailRepository.save(detail);
            });
        }

        TourBooking updated = tourBookingRepository.save(booking);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TourBooking booking = tourBookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Return seats to trip
        Trip trip = booking.getTrip();
        trip.setBookedSeats(trip.getBookedSeats() - booking.getSeatsBooked());
        tripRepository.save(trip);

        booking.markAsDeleted();
        tourBookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        TourBooking booking = tourBookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new RuntimeException("Booking is already canceled");
        }

        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed booking");
        }

        // Return seats to trip
        Trip trip = booking.getTrip();
        trip.setBookedSeats(trip.getBookedSeats() - booking.getSeatsBooked());
        tripRepository.save(trip);

        booking.setStatus(TourBooking.Status.CANCELED);
        tourBookingRepository.save(booking);

        // Update invoice status: PAID -> REFUNDED, UNPAID stays UNPAID
        invoiceRepository.findByBookingId(id).ifPresent(inv -> {
            if (inv.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
                inv.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);
                invoiceRepository.save(inv);
            }
            // If UNPAID, leave it as UNPAID (no action needed)
        });
    }

    @Override
    @Transactional
    public void removeTraveler(UUID bookingId, UUID travelerId) {
        // Get booking
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if booking is canceled or completed
        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new RuntimeException("Cannot remove traveler from canceled booking");
        }

        if (booking.getStatus() == TourBooking.Status.COMPLETED) {
            throw new RuntimeException("Cannot remove traveler from completed booking");
        }

        // Get the invoice
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Business rule: Cannot remove traveler within 3 days of departure if already
        // paid
        Trip trip = booking.getTrip();
        long daysUntilDeparture = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), trip.getDepartureDate());

        if (daysUntilDeparture < 3 && invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot remove traveler within 3 days of departure for paid bookings");
        }

        // Get the traveler
        BookingTraveler traveler = bookingTravelerRepository.findById(travelerId)
                .filter(t -> t.getDeletedAt() == 0 && t.getTourBooking().getId().equals(bookingId))
                .orElseThrow(() -> new RuntimeException("Traveler not found in this booking"));

        // Get all travelers for this booking
        List<BookingTraveler> allTravelers = bookingTravelerRepository.findByBookingId(bookingId)
                .stream()
                .filter(t -> t.getDeletedAt() == 0)
                .collect(Collectors.toList());

        // Remove traveler (soft delete)
        traveler.markAsDeleted();
        bookingTravelerRepository.save(traveler);

        // Check if this is the last traveler
        if (allTravelers.size() == 1) {
            // Cancel the entire booking
            cancel(bookingId);
            return;
        }

        // Update booking seats
        booking.setSeatsBooked(booking.getSeatsBooked() - 1);

        // Update total price
        BigDecimal newTotalPrice = trip.getPrice().multiply(BigDecimal.valueOf(booking.getSeatsBooked()));
        booking.setTotalPrice(newTotalPrice);
        tourBookingRepository.save(booking);

        // Update trip booked seats
        trip.setBookedSeats(trip.getBookedSeats() - 1);
        tripRepository.save(trip);

        // Update invoice total amount
        invoice.setTotalAmount(newTotalPrice);

        // Handle refund if already paid
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            // Calculate refund amount (1 seat price)
            BigDecimal refundAmount = trip.getPrice();
            // For simplicity, we'll mark as partially refunded
            // In real system, you'd process actual refund through payment gateway
            invoice.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);
        }

        invoiceRepository.save(invoice);
    }

    private TourBookingResponse buildResponse(TourBooking booking) {
        TourBookingResponse response = new TourBookingResponse(booking);

        // Get booking detail
        tourBookingDetailRepository.findByBookingId(booking.getId()).ifPresent(detail -> {
            response.setNoAdults(detail.getNoAdults());
            response.setNoChildren(detail.getNoChildren());
        });

        // Get travelers
        List<BookingTraveler> travelers = bookingTravelerRepository.findByBookingId(booking.getId());
        List<TourBookingResponse.TravelerInfoResponse> travelerResponses = travelers.stream()
                .map(t -> {
                    TourBookingResponse.TravelerInfoResponse info = new TourBookingResponse.TravelerInfoResponse();
                    info.setId(t.getId());
                    info.setFullName(t.getFullName());
                    info.setGender(t.getGender() != null ? t.getGender().name() : null);
                    info.setDateOfBirth(t.getDateOfBirth());
                    info.setIdentityDoc(t.getIdentityDoc());
                    info.setEmail(t.getEmail());
                    info.setPhoneNumber(t.getPhoneNumber());
                    return info;
                })
                .collect(Collectors.toList());
        response.setTravelers(travelerResponses);

        // Get invoice
        invoiceRepository.findByBookingId(booking.getId()).ifPresent(inv -> {
            TourBookingResponse.InvoiceInfoResponse invoiceInfo = new TourBookingResponse.InvoiceInfoResponse();
            invoiceInfo.setId(inv.getId());
            invoiceInfo.setTotalAmount(inv.getTotalAmount());
            invoiceInfo.setPaymentStatus(inv.getPaymentStatus().name());
            invoiceInfo.setPaymentMethod(inv.getPaymentMethod());
            response.setInvoice(invoiceInfo);
        });

        return response;
    }

    private Specification<TourBooking> buildSpecification(TourBookingFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }
            if (filter.getTripId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), filter.getTripId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Search by keyword (customer name, email, or route name)
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase().trim() + "%";
                var userJoin = root.join("user", jakarta.persistence.criteria.JoinType.LEFT);
                var tripJoin = root.join("trip", jakarta.persistence.criteria.JoinType.LEFT);
                var routeJoin = tripJoin.join("route", jakarta.persistence.criteria.JoinType.LEFT);

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("fullName")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(routeJoin.get("routeName")), keyword)));
            }

            // Filter by booking date range
            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDate.class), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDate.class), filter.getToDate()));
            }

            // Filter by departure date range
            if (filter.getDepartureFrom() != null) {
                var tripJoin = root.join("trip", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        tripJoin.get("departureDate"), filter.getDepartureFrom()));
            }
            if (filter.getDepartureTo() != null) {
                var tripJoin = root.join("trip", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        tripJoin.get("departureDate"), filter.getDepartureTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
