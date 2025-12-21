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

import com.example.tms.dto.request.tourbooking.CreateTourBookingRequest;
import com.example.tms.dto.request.tourbooking.TourBookingFilterRequest;
import com.example.tms.dto.request.tourbooking.UpdateTourBookingRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.tourbooking.TourBookingResponse;
import com.example.tms.service.interface_.TourBookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tour-bookings")
@RequiredArgsConstructor
@Tag(name = "Tour Booking Management", description = "APIs for managing tour bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class TourBookingController {

        private final TourBookingService tourBookingService;

        @Operation(summary = "Create booking", description = "Create a new tour booking")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
        })
        @PreAuthorize("isAuthenticated()")
        @PostMapping
        public ResponseEntity<ApiResponse<TourBookingResponse>> create(
                        @Valid @RequestBody CreateTourBookingRequest request) {
                TourBookingResponse response = tourBookingService.create(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Booking created successfully", response));
        }

        @Operation(summary = "Get booking by ID", description = "Retrieve booking details by ID")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TourBookingResponse>> getById(
                        @Parameter(description = "Booking ID") @PathVariable UUID id) {
                TourBookingResponse response = tourBookingService.getById(id);
                return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
        }

        @Operation(summary = "Get all bookings", description = "Retrieve a paginated list of bookings with optional filters (Admin/Staff only). "
                        +
                        "Supports search by keyword (customer name, email, route name), " +
                        "filter by status, user ID, trip ID, booking date range, and departure date range.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
        })
        @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
        @GetMapping
        public ResponseEntity<ApiResponse<PaginationResponse<TourBookingResponse>>> getAll(
                        @Parameter(description = "Filter parameters: keyword, status, userId, tripId, fromDate, toDate, departureFrom, departureTo, page, pageSize, sortBy, sortDirection") @ModelAttribute TourBookingFilterRequest filter) {
                PaginationResponse<TourBookingResponse> response = tourBookingService.getAll(filter);
                return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
        }

        @Operation(summary = "Get bookings by user ID", description = "Retrieve all bookings for a specific user")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
        })
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<TourBookingResponse>>> getByUserId(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                List<TourBookingResponse> response = tourBookingService.getByUserId(userId);
                return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
        }

        @Operation(summary = "Update booking", description = "Update booking by ID (Admin/Staff only). Can update status and/or travelers. "
                        +
                        "Travelers can only be edited before departure date and if booking is not canceled/completed.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update - trip has departed or booking is canceled/completed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TourBookingResponse>> update(
                        @Parameter(description = "Booking ID") @PathVariable UUID id,
                        @Valid @RequestBody UpdateTourBookingRequest request) {
                TourBookingResponse response = tourBookingService.update(id, request);
                return ResponseEntity.ok(ApiResponse.success("Booking updated successfully", response));
        }

        @Operation(summary = "Cancel booking", description = "Cancel a booking")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PreAuthorize("isAuthenticated()")
        @PostMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<Void>> cancel(
                        @Parameter(description = "Booking ID") @PathVariable UUID id) {
                tourBookingService.cancel(id);
                return ResponseEntity.ok(ApiResponse.success("Booking canceled successfully"));
        }

        @Operation(summary = "Delete booking", description = "Delete booking by ID (Admin only)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking deleted successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PreAuthorize("hasAuthority('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @Parameter(description = "Booking ID") @PathVariable UUID id) {
                tourBookingService.delete(id);
                return ResponseEntity.ok(ApiResponse.success("Booking deleted successfully"));
        }

        @Operation(summary = "Remove traveler from booking", description = "Remove a specific traveler from a booking (Admin/Staff only). "
                        +
                        "Validates business rules: cannot remove within 3 days of departure if booking is paid. " +
                        "If this is the last traveler, the entire booking will be canceled.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Traveler removed successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot remove traveler - business rule violation"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking or traveler not found")
        })
        @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
        @DeleteMapping("/{bookingId}/travelers/{travelerId}")
        public ResponseEntity<ApiResponse<Void>> removeTraveler(
                        @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
                        @Parameter(description = "Traveler ID") @PathVariable UUID travelerId) {
                tourBookingService.removeTraveler(bookingId, travelerId);
                return ResponseEntity.ok(ApiResponse.success("Traveler removed successfully"));
        }
}
