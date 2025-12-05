package com.example.tms.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.invoice.CreateInvoiceRequest;
import com.example.tms.dto.request.invoice.InvoiceFilterRequest;
import com.example.tms.dto.request.invoice.UpdateInvoiceRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.invoice.InvoiceResponse;
import com.example.tms.service.interface_.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "APIs for managing invoices")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Create invoice", description = "Create a new invoice (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Invoice created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", response));
    }

    @Operation(summary = "Get invoice by ID", description = "Retrieve invoice details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {
        InvoiceResponse response = invoiceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", response));
    }

    @Operation(summary = "Get invoice by booking ID", description = "Retrieve invoice for a specific booking")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByBookingId(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        InvoiceResponse response = invoiceService.getByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", response));
    }

    @Operation(summary = "Get all invoices", description = "Retrieve a paginated list of invoices with optional filters (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoices retrieved successfully")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<InvoiceResponse>>> getAll(
            @ModelAttribute InvoiceFilterRequest filter) {
        PaginationResponse<InvoiceResponse> response = invoiceService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", response));
    }

    @Operation(summary = "Update invoice", description = "Update invoice by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", response));
    }

    @Operation(summary = "Mark invoice as paid", description = "Mark an invoice as paid (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice marked as paid successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @Parameter(description = "Payment method") @RequestParam(defaultValue = "CASH") String paymentMethod) {
        InvoiceResponse response = invoiceService.markAsPaid(id, paymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid successfully", response));
    }

    @Operation(summary = "Mark invoice as refunded", description = "Mark an invoice as refunded (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice marked as refunded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsRefunded(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {
        InvoiceResponse response = invoiceService.markAsRefunded(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as refunded successfully", response));
    }

    @Operation(summary = "Delete invoice", description = "Delete invoice by ID (Admin only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully"));
    }
}
