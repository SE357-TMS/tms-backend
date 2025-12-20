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

import com.example.tms.dto.request.trip.CreateTripRequest;
import com.example.tms.dto.request.trip.TripFilterRequest;
import com.example.tms.dto.request.trip.UpdateTripRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.trip.TripAvailableDatesResponse;
import com.example.tms.dto.response.trip.TripResponse;
import com.example.tms.service.interface_.TripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "APIs for managing trips")
public class TripController {

    private final TripService tripService;

    @Operation(summary = "Create trip", description = "Create a new trip (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Trip created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> create(@Valid @RequestBody CreateTripRequest request) {
        TripResponse response = tripService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", response));
    }

    @Operation(summary = "Get trip by ID", description = "Retrieve trip details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getById(
            @Parameter(description = "Trip ID") @PathVariable UUID id) {
        TripResponse response = tripService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Trip retrieved successfully", response));
    }

    @Operation(summary = "Get all trips", description = "Retrieve a paginated list of trips with optional filters")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trips retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<TripResponse>>> getAll(
            @ModelAttribute TripFilterRequest filter) {
        PaginationResponse<TripResponse> response = tripService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Trips retrieved successfully", response));
    }

    @Operation(summary = "Get all trips (no pagination)", description = "Retrieve all trips without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAllNoPagination() {
        List<TripResponse> response = tripService.getAllNoPagination();
        return ResponseEntity.ok(ApiResponse.success("Trips retrieved successfully", response));
    }

    @Operation(summary = "Update trip", description = "Update trip by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> update(
            @Parameter(description = "Trip ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequest request) {
        TripResponse response = tripService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", response));
    }

    @Operation(summary = "Delete trip", description = "Delete trip by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Trip ID") @PathVariable UUID id) {
        tripService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully"));
    }
    
    @Operation(summary = "Get available trips by route", description = "Get all available trips for a route (departure date >= today + 3 days)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Available trips retrieved successfully")
    })
    @GetMapping("/route/{routeId}/available")
    public ResponseEntity<ApiResponse<List<TripAvailableDatesResponse>>> getAvailableTripsByRoute(
            @Parameter(description = "Route ID") @PathVariable UUID routeId) {
        List<TripAvailableDatesResponse> response = tripService.getAvailableTripsByRouteId(routeId);
        return ResponseEntity.ok(ApiResponse.success("Available trips retrieved successfully", response));
    }
    
    @Operation(summary = "Get nearest available trip", description = "Get the nearest available trip for a route")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Nearest trip retrieved successfully")
    })
    @GetMapping("/route/{routeId}/nearest")
    public ResponseEntity<ApiResponse<TripAvailableDatesResponse>> getNearestAvailableTrip(
            @Parameter(description = "Route ID") @PathVariable UUID routeId) {
        TripAvailableDatesResponse response = tripService.getNearestAvailableTrip(routeId);
        return ResponseEntity.ok(ApiResponse.success("Nearest trip retrieved successfully", response));
    }
}

