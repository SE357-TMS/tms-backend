package com.example.tms.service.impl;

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

import com.example.tms.dto.request.routeattraction.CreateRouteAttractionRequest;
import com.example.tms.dto.request.routeattraction.RouteAttractionFilterRequest;
import com.example.tms.dto.request.routeattraction.UpdateRouteAttractionRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.routeattraction.RouteAttractionResponse;
import com.example.tms.enity.Attraction;
import com.example.tms.enity.Route;
import com.example.tms.enity.RouteAttraction;
import com.example.tms.repository.AttractionRepository;
import com.example.tms.repository.RouteAttractionRepository;
import com.example.tms.repository.RouteRepository;
import com.example.tms.service.interface_.RouteAttractionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteAttractionServiceImpl implements RouteAttractionService {

    private final RouteAttractionRepository routeAttractionRepository;
    private final RouteRepository routeRepository;
    private final AttractionRepository attractionRepository;

    @Override
    @Transactional
    public RouteAttractionResponse create(CreateRouteAttractionRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .filter(r -> r.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        Attraction attraction = attractionRepository.findById(request.getAttractionId())
                .filter(a -> a.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Attraction not found"));

        RouteAttraction routeAttraction = new RouteAttraction();
        routeAttraction.setRoute(route);
        routeAttraction.setAttraction(attraction);
        routeAttraction.setDay(request.getDay());
        routeAttraction.setOrderInDay(request.getOrderInDay());
        routeAttraction.setActivityDescription(request.getActivityDescription());

        RouteAttraction saved = routeAttractionRepository.save(routeAttraction);
        return new RouteAttractionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteAttractionResponse getById(UUID id) {
        RouteAttraction routeAttraction = routeAttractionRepository.findById(id)
                .filter(ra -> ra.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route attraction not found"));
        return new RouteAttractionResponse(routeAttraction);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RouteAttractionResponse> getAll(RouteAttractionFilterRequest filter) {
        Sort.Order dayOrder = new Sort.Order(Sort.Direction.ASC, "day");
        Sort.Order orderInDayOrder = new Sort.Order(Sort.Direction.ASC, "orderInDay");
        Sort sort = Sort.by(dayOrder, orderInDayOrder);
        
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<RouteAttraction> spec = buildSpecification(filter);
        Page<RouteAttraction> page = routeAttractionRepository.findAll(spec, pageable);

        List<RouteAttractionResponse> items = page.getContent().stream()
                .map(RouteAttractionResponse::new)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteAttractionResponse> getByRouteId(UUID routeId) {
        return routeAttractionRepository.findAll().stream()
                .filter(ra -> ra.getDeletedAt() == 0)
                .filter(ra -> ra.getRoute().getId().equals(routeId))
                .sorted((a, b) -> {
                    int dayCompare = a.getDay().compareTo(b.getDay());
                    if (dayCompare != 0) return dayCompare;
                    return a.getOrderInDay().compareTo(b.getOrderInDay());
                })
                .map(RouteAttractionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RouteAttractionResponse update(UUID id, UpdateRouteAttractionRequest request) {
        RouteAttraction routeAttraction = routeAttractionRepository.findById(id)
                .filter(ra -> ra.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route attraction not found"));

        if (request.getRouteId() != null) {
            Route route = routeRepository.findById(request.getRouteId())
                    .filter(r -> r.getDeletedAt() == 0)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            routeAttraction.setRoute(route);
        }
        if (request.getAttractionId() != null) {
            Attraction attraction = attractionRepository.findById(request.getAttractionId())
                    .filter(a -> a.getDeletedAt() == 0)
                    .orElseThrow(() -> new RuntimeException("Attraction not found"));
            routeAttraction.setAttraction(attraction);
        }
        if (request.getDay() != null) {
            routeAttraction.setDay(request.getDay());
        }
        if (request.getOrderInDay() != null) {
            routeAttraction.setOrderInDay(request.getOrderInDay());
        }
        if (request.getActivityDescription() != null) {
            routeAttraction.setActivityDescription(request.getActivityDescription());
        }

        RouteAttraction updated = routeAttractionRepository.save(routeAttraction);
        return new RouteAttractionResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        RouteAttraction routeAttraction = routeAttractionRepository.findById(id)
                .filter(ra -> ra.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Route attraction not found"));
        routeAttraction.markAsDeleted();
        routeAttractionRepository.save(routeAttraction);
    }

    private Specification<RouteAttraction> buildSpecification(RouteAttractionFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getRouteId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("route").get("id"), filter.getRouteId()));
            }
            if (filter.getAttractionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("attraction").get("id"), filter.getAttractionId()));
            }
            if (filter.getDay() != null) {
                predicates.add(criteriaBuilder.equal(root.get("day"), filter.getDay()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
