package com.example.tms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.bookingtraveler.CreateBookingTravelerRequest;
import com.example.tms.dto.request.bookingtraveler.UpdateBookingTravelerRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.bookingtraveler.BookingTravelerResponse;
import com.example.tms.service.interface_.BookingTravelerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/booking-travelers")
@RequiredArgsConstructor
@Tag(name = "Booking Traveler Management", description = "APIs for managing booking travelers")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingTravelerController {

    private final BookingTravelerService bookingTravelerService;

    @Operation(summary = "Create booking traveler", description = "Add a traveler to a booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking traveler created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingTravelerResponse>> create(@Valid @RequestBody CreateBookingTravelerRequest request) {
        BookingTravelerResponse response = bookingTravelerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking traveler created successfully", response));
    }

    @Operation(summary = "Get booking traveler by ID", description = "Retrieve booking traveler details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking traveler retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking traveler not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingTravelerResponse>> getById(
            @Parameter(description = "Booking Traveler ID") @PathVariable UUID id) {
        BookingTravelerResponse response = bookingTravelerService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking traveler retrieved successfully", response));
    }

    @Operation(summary = "Get travelers by booking ID", description = "Retrieve all travelers for a specific booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking travelers retrieved successfully")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<BookingTravelerResponse>>> getByBookingId(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        List<BookingTravelerResponse> response = bookingTravelerService.getByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking travelers retrieved successfully", response));
    }

    @Operation(summary = "Update booking traveler", description = "Update booking traveler by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking traveler updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking traveler not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingTravelerResponse>> update(
            @Parameter(description = "Booking Traveler ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingTravelerRequest request) {
        BookingTravelerResponse response = bookingTravelerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking traveler updated successfully", response));
    }

    @Operation(summary = "Delete booking traveler", description = "Remove a traveler from a booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking traveler deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking traveler not found")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Booking Traveler ID") @PathVariable UUID id) {
        bookingTravelerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Booking traveler deleted successfully"));
    }
}

