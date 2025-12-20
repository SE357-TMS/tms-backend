package com.example.tms.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.route.CreateRouteRequest;
import com.example.tms.dto.request.route.RouteFilterRequest;
import com.example.tms.dto.request.route.UpdateRouteRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.route.RouteDetailResponse;
import com.example.tms.dto.response.route.RouteFullDetailResponse;
import com.example.tms.dto.response.route.RouteResponse;
import com.example.tms.entity.Route;
import com.example.tms.entity.RouteAttraction;
import com.example.tms.entity.Trip;
import com.example.tms.repository.RouteAttractionRepository;
import com.example.tms.repository.RouteRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.service.interface_.CloudinaryService;
import com.example.tms.service.interface_.RouteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteAttractionRepository routeAttractionRepository;
    private final TripRepository tripRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public RouteResponse create(CreateRouteRequest request) {
        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setStartLocation(request.getStartLocation());
        route.setEndLocation(request.getEndLocation());
        route.setDurationDays(request.getDurationDays());
        route.setImage(request.getImage());
        route.setStatus(Route.Status.OPEN);

        Route saved = routeRepository.save(route);
        return new RouteResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getById(UUID id) {
        Route route = routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        return new RouteResponse(route);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RouteDetailResponse getDetailById(UUID id) {
        Route route = routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        RouteDetailResponse response = new RouteDetailResponse(route);
        
        // Get images from Cloudinary
        List<String> images = cloudinaryService.getRouteImages(id);
        response.setImages(images);
        
        // Get itinerary (route attractions grouped by day)
        List<RouteAttraction> attractions = routeAttractionRepository.findByRouteIdOrderByDayAndOrder(id);
        
        // Group by day
        Map<Integer, List<RouteAttraction>> groupedByDay = attractions.stream()
                .collect(Collectors.groupingBy(RouteAttraction::getDay));
        
        List<RouteDetailResponse.ItineraryDay> itinerary = new ArrayList<>();
        for (int day = 1; day <= (route.getDurationDays() != null ? route.getDurationDays() : 0); day++) {
            RouteDetailResponse.ItineraryDay itineraryDay = new RouteDetailResponse.ItineraryDay();
            itineraryDay.setDay(day);
            
            List<RouteAttraction> dayAttractions = groupedByDay.getOrDefault(day, new ArrayList<>());
            List<RouteDetailResponse.ItineraryAttraction> attractionList = dayAttractions.stream()
                    .map(ra -> {
                        RouteDetailResponse.ItineraryAttraction ia = new RouteDetailResponse.ItineraryAttraction();
                        ia.setAttractionId(ra.getAttraction().getId());
                        ia.setAttractionName(ra.getAttraction().getName());
                        ia.setLocation(ra.getAttraction().getLocation());
                        if (ra.getAttraction().getCategory() != null) {
                            ia.setCategoryName(ra.getAttraction().getCategory().getName());
                        }
                        ia.setOrderInDay(ra.getOrderInDay());
                        ia.setActivityDescription(ra.getActivityDescription());
                        return ia;
                    })
                    .sorted((a, b) -> a.getOrderInDay().compareTo(b.getOrderInDay()))
                    .collect(Collectors.toList());
            
            itineraryDay.setAttractions(attractionList);
            itinerary.add(itineraryDay);
        }
        
        response.setItinerary(itinerary);
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public RouteFullDetailResponse getFullDetailById(UUID id) {
        Route route = routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        RouteFullDetailResponse response = new RouteFullDetailResponse(route);
        
        // Get available trips (departure date >= today + 3 days)
        LocalDate minDate = LocalDate.now().plusDays(3);
        List<Trip> trips = tripRepository.findAvailableTripsByRouteId(id, minDate);
        List<RouteFullDetailResponse.AvailableTrip> availableTrips = trips.stream()
                .filter(t -> t.getTotalSeats() - t.getBookedSeats() > 0)
                .map(RouteFullDetailResponse.AvailableTrip::new)
                .collect(Collectors.toList());
        response.setAvailableTrips(availableTrips);
        
        // Get itinerary (route attractions grouped by day)
        List<RouteAttraction> attractions = routeAttractionRepository.findByRouteIdOrderByDayAndOrder(id);
        Map<Integer, List<RouteAttraction>> groupedByDay = attractions.stream()
                .collect(Collectors.groupingBy(RouteAttraction::getDay));
        
        List<RouteFullDetailResponse.ItineraryDay> itinerary = new ArrayList<>();
        for (int day = 1; day <= (route.getDurationDays() != null ? route.getDurationDays() : 0); day++) {
            RouteFullDetailResponse.ItineraryDay itineraryDay = new RouteFullDetailResponse.ItineraryDay();
            itineraryDay.setDay(day);
            
            List<RouteAttraction> dayAttractions = groupedByDay.getOrDefault(day, new ArrayList<>());
            List<RouteFullDetailResponse.ItineraryAttraction> attractionList = dayAttractions.stream()
                    .map(ra -> {
                        RouteFullDetailResponse.ItineraryAttraction ia = new RouteFullDetailResponse.ItineraryAttraction();
                        ia.setAttractionId(ra.getAttraction().getId());
                        ia.setAttractionName(ra.getAttraction().getName());
                        ia.setLocation(ra.getAttraction().getLocation());
                        if (ra.getAttraction().getCategory() != null) {
                            ia.setCategoryName(ra.getAttraction().getCategory().getName());
                        }
                        ia.setOrderInDay(ra.getOrderInDay());
                        ia.setActivityDescription(ra.getActivityDescription());
                        return ia;
                    })
                    .sorted((a, b) -> a.getOrderInDay().compareTo(b.getOrderInDay()))
                    .collect(Collectors.toList());
            
            itineraryDay.setAttractions(attractionList);
            itinerary.add(itineraryDay);
        }
        
        response.setItinerary(itinerary);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRouteImages(UUID id) {
        routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        return cloudinaryService.getRouteImages(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RouteResponse> getAll(RouteFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<Route> spec = buildSpecification(filter);
        Page<Route> page = routeRepository.findAll(spec, pageable);

        List<RouteResponse> items = page.getContent().stream()
                .map(RouteResponse::new)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllNoPagination() {
        return routeRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == 0)
                .map(RouteResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RouteResponse update(UUID id, UpdateRouteRequest request) {
        Route route = routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (request.getRouteName() != null) {
            route.setRouteName(request.getRouteName());
        }
        if (request.getStartLocation() != null) {
            route.setStartLocation(request.getStartLocation());
        }
        if (request.getEndLocation() != null) {
            route.setEndLocation(request.getEndLocation());
        }
        if (request.getDurationDays() != null) {
            route.setDurationDays(request.getDurationDays());
        }
        if (request.getImage() != null) {
            route.setImage(request.getImage());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }

        Route updated = routeRepository.save(route);
        return new RouteResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Route route = routeRepository.findById(id)
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        route.markAsDeleted();
        routeRepository.save(route);
    }

    private Specification<Route> buildSpecification(RouteFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getRouteName() != null && !filter.getRouteName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("routeName")),
                        "%" + filter.getRouteName().toLowerCase() + "%"
                ));
            }
            if (filter.getStartLocation() != null && !filter.getStartLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("startLocation")),
                        "%" + filter.getStartLocation().toLowerCase() + "%"
                ));
            }
            if (filter.getEndLocation() != null && !filter.getEndLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("endLocation")),
                        "%" + filter.getEndLocation().toLowerCase() + "%"
                ));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

