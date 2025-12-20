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
                filter.getSortBy()
        );
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

        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
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

        // Update invoice status if paid
        invoiceRepository.findByBookingId(id).ifPresent(invoice -> {
            if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
                invoice.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);
                invoiceRepository.save(invoice);
            }
        });
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
                    return info;
                })
                .collect(Collectors.toList());
        response.setTravelers(travelerResponses);

        // Get invoice
        invoiceRepository.findByBookingId(booking.getId()).ifPresent(invoice -> {
            TourBookingResponse.InvoiceInfoResponse invoiceInfo = new TourBookingResponse.InvoiceInfoResponse();
            invoiceInfo.setId(invoice.getId());
            invoiceInfo.setTotalAmount(invoice.getTotalAmount());
            invoiceInfo.setPaymentStatus(invoice.getPaymentStatus().name());
            invoiceInfo.setPaymentMethod(invoice.getPaymentMethod());
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

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

