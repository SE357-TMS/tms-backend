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

import com.example.tms.dto.request.route.CreateRouteRequest;
import com.example.tms.dto.request.route.RouteFilterRequest;
import com.example.tms.dto.request.route.UpdateRouteRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.route.RouteResponse;
import com.example.tms.enity.Route;
import com.example.tms.repository.RouteRepository;
import com.example.tms.service.interface_.RouteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

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
