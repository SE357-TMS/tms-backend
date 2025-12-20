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

import com.example.tms.dto.request.attraction.AttractionFilterRequest;
import com.example.tms.dto.request.attraction.CreateAttractionRequest;
import com.example.tms.dto.request.attraction.UpdateAttractionRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.attraction.AttractionResponse;
import com.example.tms.service.interface_.AttractionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attractions")
@RequiredArgsConstructor
@Tag(name = "Attraction Management", description = "APIs for managing attractions")
public class AttractionController {

    private final AttractionService attractionService;

    @Operation(summary = "Create attraction", description = "Create a new attraction (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Attraction created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<AttractionResponse>> create(@Valid @RequestBody CreateAttractionRequest request) {
        AttractionResponse response = attractionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attraction created successfully", response));
    }

    @Operation(summary = "Get attraction by ID", description = "Retrieve attraction details by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attraction retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attraction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttractionResponse>> getById(
            @Parameter(description = "Attraction ID") @PathVariable UUID id) {
        AttractionResponse response = attractionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Attraction retrieved successfully", response));
    }

    @Operation(summary = "Get all attractions", description = "Retrieve a paginated list of attractions with optional filters")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attractions retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<AttractionResponse>>> getAll(
            @ModelAttribute AttractionFilterRequest filter) {
        PaginationResponse<AttractionResponse> response = attractionService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Attractions retrieved successfully", response));
    }

    @Operation(summary = "Get all attractions (no pagination)", description = "Retrieve all attractions without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AttractionResponse>>> getAllNoPagination() {
        List<AttractionResponse> response = attractionService.getAllNoPagination();
        return ResponseEntity.ok(ApiResponse.success("Attractions retrieved successfully", response));
    }

    @Operation(summary = "Update attraction", description = "Update attraction by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attraction updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attraction not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttractionResponse>> update(
            @Parameter(description = "Attraction ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateAttractionRequest request) {
        AttractionResponse response = attractionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attraction updated successfully", response));
    }

    @Operation(summary = "Delete attraction", description = "Delete attraction by ID (Admin/Staff only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attraction deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attraction not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Attraction ID") @PathVariable UUID id) {
        attractionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attraction deleted successfully"));
    }
}

