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

import com.example.tms.dto.request.routeattraction.CreateRouteAttractionRequest;
import com.example.tms.dto.request.routeattraction.RouteAttractionFilterRequest;
import com.example.tms.dto.request.routeattraction.UpdateRouteAttractionRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.routeattraction.RouteAttractionResponse;
import com.example.tms.service.interface_.RouteAttractionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/route-attractions")
@RequiredArgsConstructor
@Tag(name = "Route Attraction Management", description = "APIs for managing route attractions")
public class RouteAttractionController {

    private final RouteAttractionService routeAttractionService;

    @Operation(summary = "Create route attraction", description = "Add an attraction to a route (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Route attraction created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<RouteAttractionResponse>> create(@Valid @RequestBody CreateRouteAttractionRequest request) {
        RouteAttractionResponse response = routeAttractionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route attraction created successfully", response));
    }

    @Operation(summary = "Get route attraction by ID", description = "Retrieve route attraction details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route attraction retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route attraction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteAttractionResponse>> getById(
            @Parameter(description = "Route Attraction ID") @PathVariable UUID id) {
        RouteAttractionResponse response = routeAttractionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Route attraction retrieved successfully", response));
    }

    @Operation(summary = "Get all route attractions", description = "Retrieve a paginated list of route attractions with optional filters")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route attractions retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<RouteAttractionResponse>>> getAll(
            @ModelAttribute RouteAttractionFilterRequest filter) {
        PaginationResponse<RouteAttractionResponse> response = routeAttractionService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Route attractions retrieved successfully", response));
    }

    @Operation(summary = "Get attractions by route ID", description = "Retrieve all attractions for a specific route")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route attractions retrieved successfully")
    })
    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<RouteAttractionResponse>>> getByRouteId(
            @Parameter(description = "Route ID") @PathVariable UUID routeId) {
        List<RouteAttractionResponse> response = routeAttractionService.getByRouteId(routeId);
        return ResponseEntity.ok(ApiResponse.success("Route attractions retrieved successfully", response));
    }

    @Operation(summary = "Update route attraction", description = "Update route attraction by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route attraction updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route attraction not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteAttractionResponse>> update(
            @Parameter(description = "Route Attraction ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteAttractionRequest request) {
        RouteAttractionResponse response = routeAttractionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Route attraction updated successfully", response));
    }

    @Operation(summary = "Delete route attraction", description = "Remove an attraction from a route (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route attraction deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Route attraction not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Route Attraction ID") @PathVariable UUID id) {
        routeAttractionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Route attraction deleted successfully"));
    }
}
