package com.example.tms.controller;

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

import com.example.tms.dto.request.bookingdetail.CreateBookingDetailRequest;
import com.example.tms.dto.request.bookingdetail.UpdateBookingDetailRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.bookingdetail.BookingDetailResponse;
import com.example.tms.service.interface_.BookingDetailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/booking-details")
@RequiredArgsConstructor
@Tag(name = "Booking Detail Management", description = "APIs for managing booking details")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingDetailController {

    private final BookingDetailService bookingDetailService;

    @Operation(summary = "Create booking detail", description = "Create booking detail for a booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking detail created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingDetailResponse>> create(@Valid @RequestBody CreateBookingDetailRequest request) {
        BookingDetailResponse response = bookingDetailService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking detail created successfully", response));
    }

    @Operation(summary = "Get booking detail by ID", description = "Retrieve booking detail by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking detail retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking detail not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getById(
            @Parameter(description = "Booking Detail ID") @PathVariable UUID id) {
        BookingDetailResponse response = bookingDetailService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking detail retrieved successfully", response));
    }

    @Operation(summary = "Get booking detail by booking ID", description = "Retrieve booking detail for a specific booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking detail retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking detail not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getByBookingId(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        BookingDetailResponse response = bookingDetailService.getByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking detail retrieved successfully", response));
    }

    @Operation(summary = "Update booking detail", description = "Update booking detail by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking detail updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking detail not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> update(
            @Parameter(description = "Booking Detail ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingDetailRequest request) {
        BookingDetailResponse response = bookingDetailService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking detail updated successfully", response));
    }

    @Operation(summary = "Delete booking detail", description = "Delete booking detail by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking detail deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking detail not found")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Booking Detail ID") @PathVariable UUID id) {
        bookingDetailService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Booking detail deleted successfully"));
    }
}
