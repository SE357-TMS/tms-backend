package com.example.tms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.route.CreateRouteRequest;
import com.example.tms.dto.request.route.RouteFilterRequest;
import com.example.tms.dto.request.route.UpdateRouteRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.route.RouteDetailResponse;
import com.example.tms.dto.response.route.RouteResponse;
import com.example.tms.service.interface_.RouteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Route Management", description = "APIs for managing routes")
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "Create route", description = "Create a new route (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Route created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> create(@Valid @RequestBody CreateRouteRequest request) {
        RouteResponse response = routeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route created successfully", response));
    }

    @Operation(summary = "Get route by ID", description = "Retrieve route details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getById(
            @Parameter(description = "Route ID") @PathVariable UUID id) {
        RouteResponse response = routeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Route retrieved successfully", response));
    }
    
    @Operation(summary = "Get route detail by ID", description = "Retrieve full route details including itinerary and images")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route detail retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<RouteDetailResponse>> getDetailById(
            @Parameter(description = "Route ID") @PathVariable UUID id) {
        RouteDetailResponse response = routeService.getDetailById(id);
        return ResponseEntity.ok(ApiResponse.success("Route detail retrieved successfully", response));
    }

    @Operation(summary = "Get all routes", description = "Retrieve a paginated list of routes with optional filters")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Routes retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<RouteResponse>>> getAll(
            @ModelAttribute RouteFilterRequest filter) {
        PaginationResponse<RouteResponse> response = routeService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Routes retrieved successfully", response));
    }

    @Operation(summary = "Get all routes (no pagination)", description = "Retrieve all routes without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getAllNoPagination() {
        List<RouteResponse> response = routeService.getAllNoPagination();
        return ResponseEntity.ok(ApiResponse.success("Routes retrieved successfully", response));
    }

    @Operation(summary = "Update route", description = "Update route by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> update(
            @Parameter(description = "Route ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request) {
        RouteResponse response = routeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Route updated successfully", response));
    }

    @Operation(summary = "Delete route", description = "Delete route by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Route ID") @PathVariable UUID id) {
        routeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted successfully"));
    }
}
