package com.example.tms.controller.staff;

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

import com.example.tms.dto.paging.PagedResponseDto;
import com.example.tms.dto.request.staff.AddStaffRequest;
import com.example.tms.dto.request.staff.StaffFilterRequest;
import com.example.tms.dto.request.staff.UpdateStaffRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.staff.StaffDetailResponse;
import com.example.tms.dto.response.staff.StaffListResponse;
import com.example.tms.service.interface_.StaffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/staffs")
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "APIs for managing staff members")
@SecurityRequirement(name = "Bearer Authentication")
public class StaffController {

    private final StaffService staffService;

    @Operation(
        summary = "Get all staffs",
        description = "Retrieve a paginated list of all staff members with optional filters"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staffs retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<StaffListResponse>>> getAllStaffs(
            @ModelAttribute StaffFilterRequest filter) {
        PagedResponseDto<StaffListResponse> response = staffService.getAllStaffs(filter);
        
        if (response.getContent().isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.success("No staff members found matching the criteria", response)
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Staffs retrieved successfully", response)
        );
    }

    @Operation(
        summary = "Get staff by ID",
        description = "Retrieve detailed information about a specific staff member"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDetailResponse>> getStaffById(
            @Parameter(description = "Staff ID", required = true) @PathVariable UUID id) {
        StaffDetailResponse response = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", response));
    }

    @Operation(
        summary = "Create new staff",
        description = "Create a new staff member account with the provided information"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Staff created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<StaffDetailResponse>> createStaff(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Staff information",
                required = true
            )
            @Valid @RequestBody AddStaffRequest request) {
        StaffDetailResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff created successfully", response));
    }

    @Operation(
        summary = "Update staff",
        description = "Update information of an existing staff member"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDetailResponse>> updateStaff(
            @Parameter(description = "Staff ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated staff information",
                required = true
            )
            @Valid @RequestBody UpdateStaffRequest request) {
        StaffDetailResponse response = staffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", response));
    }

    @Operation(
        summary = "Delete staff",
        description = "Permanently delete a staff member account"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @Parameter(description = "Staff ID", required = true) @PathVariable UUID id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Staff deleted successfully"));
    }
}

