package com.example.tms.service.impl;

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

import com.example.tms.dto.request.trip.CreateTripRequest;
import com.example.tms.dto.request.trip.TripFilterRequest;
import com.example.tms.dto.request.trip.UpdateTripRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.trip.TripAvailableDatesResponse;
import com.example.tms.dto.response.trip.TripResponse;
import com.example.tms.entity.Route;
import com.example.tms.entity.TourBooking;
import com.example.tms.entity.Trip;
import com.example.tms.repository.RouteRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.service.interface_.TripService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    @Transactional
    public TripResponse create(CreateTripRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (request.getReturnDate().isBefore(request.getDepartureDate())) {
            throw new RuntimeException("Return date must be after departure date");
        }

        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setDepartureDate(request.getDepartureDate());
        trip.setReturnDate(request.getReturnDate());
        trip.setPrice(request.getPrice());
        trip.setTotalSeats(request.getTotalSeats());
        trip.setBookedSeats(0);
        trip.setPickUpTime(request.getPickUpTime());
        trip.setPickUpLocation(request.getPickUpLocation());
        trip.setStatus(Trip.Status.SCHEDULED);

        Trip saved = tripRepository.save(trip);
        return new TripResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getById(UUID id) {
        Trip trip = tripRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return new TripResponse(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<TripResponse> getAll(TripFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<Trip> spec = buildSpecification(filter);
        Page<Trip> page = tripRepository.findAll(spec, pageable);

        List<TripResponse> items = page.getContent().stream()
                .map(TripResponse::new)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllNoPagination() {
        return tripRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == 0)
                .map(TripResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TripResponse update(UUID id, UpdateTripRequest request) {
        Trip trip = tripRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (request.getRouteId() != null) {
            Route route = routeRepository.findById(request.getRouteId())
                    .filter(r -> r.getDeletedAt() == 0)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            trip.setRoute(route);
        }
        if (request.getDepartureDate() != null) {
            trip.setDepartureDate(request.getDepartureDate());
        }
        if (request.getReturnDate() != null) {
            trip.setReturnDate(request.getReturnDate());
        }
        if (request.getPrice() != null) {
            trip.setPrice(request.getPrice());
        }
        if (request.getTotalSeats() != null) {
            if (request.getTotalSeats() < trip.getBookedSeats()) {
                throw new RuntimeException("Total seats cannot be less than booked seats");
            }
            trip.setTotalSeats(request.getTotalSeats());
        }
        if (request.getPickUpTime() != null) {
            trip.setPickUpTime(request.getPickUpTime());
        }
        if (request.getPickUpLocation() != null) {
            trip.setPickUpLocation(request.getPickUpLocation());
        }
        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }

        // Validate dates
        if (trip.getReturnDate().isBefore(trip.getDepartureDate())) {
            throw new RuntimeException("Return date must be after departure date");
        }

        Trip updated = tripRepository.save(trip);
        return new TripResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Trip trip = tripRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getBookedSeats() > 0) {
            throw new RuntimeException("Cannot delete trip with existing bookings");
        }

        trip.markAsDeleted();
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public TripResponse cancelTrip(UUID id) {
        Trip trip = tripRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() == Trip.Status.CANCELED) {
            throw new RuntimeException("Trip is already canceled");
        }

        if (trip.getStatus() == Trip.Status.FINISHED) {
            throw new RuntimeException("Cannot cancel a finished trip");
        }

        // Cancel the trip
        trip.setStatus(Trip.Status.CANCELED);
        Trip updated = tripRepository.save(trip);

        // Find all active bookings for this trip and cancel them
        List<TourBooking> activeBookings = tourBookingRepository.findByTripId(id).stream()
                .filter(booking -> booking.getStatus() != TourBooking.Status.CANCELED
                        && booking.getStatus() != TourBooking.Status.COMPLETED)
                .collect(Collectors.toList());

        if (!activeBookings.isEmpty()) {
            log.info("Canceling {} active bookings for trip {}", activeBookings.size(), id);

            // Cancel all active bookings
            for (TourBooking booking : activeBookings) {
                booking.setStatus(TourBooking.Status.CANCELED);
                tourBookingRepository.save(booking);
                log.info("Booking {} automatically canceled due to trip cancellation", booking.getId());
            }

            // TODO: In a real system, you should:
            // 1. Process refunds for paid bookings based on refund policy
            // 2. Send email notifications to affected customers
            // 3. Create refund records in the system
            // 4. Update invoice statuses if needed
        }

        log.info("Trip {} canceled successfully with {} bookings affected", id, activeBookings.size());
        return new TripResponse(updated);
    }

    private Specification<Trip> buildSpecification(TripFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getRouteId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("route").get("id"), filter.getRouteId()));
            }
            if (filter.getDepartureDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("departureDate"), filter.getDepartureDateFrom()));
            }
            if (filter.getDepartureDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("departureDate"), filter.getDepartureDateTo()));
            }
            if (filter.getReturnDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("returnDate"), filter.getReturnDateFrom()));
            }
            if (filter.getReturnDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("returnDate"), filter.getReturnDateTo()));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getHasAvailableSeats() != null && filter.getHasAvailableSeats()) {
                predicates.add(criteriaBuilder.greaterThan(
                        criteriaBuilder.diff(root.get("totalSeats"), root.get("bookedSeats")), 0));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripAvailableDatesResponse> getAvailableTripsByRouteId(UUID routeId) {
        // Verify route exists
        routeRepository.findById(routeId)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        // Get trips with departure date >= today + 3 days
        LocalDate minDate = LocalDate.now().plusDays(3);
        List<Trip> trips = tripRepository.findAvailableTripsByRouteId(routeId, minDate);

        return trips.stream()
                .map(TripAvailableDatesResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripAvailableDatesResponse getNearestAvailableTrip(UUID routeId) {
        // Verify route exists
        routeRepository.findById(routeId)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        LocalDate minDate = LocalDate.now().plusDays(3);
        return tripRepository.findNearestAvailableTrip(routeId, minDate)
                .map(TripAvailableDatesResponse::new)
                .orElse(null);
    }
}
