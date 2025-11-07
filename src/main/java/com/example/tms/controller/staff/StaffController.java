package com.example.tms.controller.staff;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Staff Management Controller
 * Handles all CRUD operations for staff management
 * Only accessible by ADMIN role
 */
@RestController
@RequestMapping("/admin/staffs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final StaffService staffService;

    /**
     * UC_STAFF_01: View and Filter Staffs
     * GET /admin/staffs
     * 
     * Query Parameters:
     * - keyword: Search by username, full_name, or email
     * - phoneNumber: Filter by phone number
     * - isLock: Filter by lock status (true/false)
     * - gender: Filter by gender (M/F/O)
     * - page: Page number (default: 0)
     * - size: Page size (default: 10)
     * - sortBy: Sort field (default: id)
     * - sortDirection: Sort direction (default: DESC)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<StaffListResponse>>> getAllStaffs(
            @ModelAttribute StaffFilterRequest filter) {
        log.info("Fetching staffs with filter: {}", filter);
        
        PagedResponseDto<StaffListResponse> staffs = staffService.getAllStaffs(filter);
        
        if (staffs.getContent().isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.success("No staff members found matching the criteria", staffs)
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Staff list retrieved successfully", staffs)
        );
    }

    /**
     * UC_STAFF_02: View Staff Details
     * GET /admin/staffs/{staffId}
     */
    @GetMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffDetailResponse>> getStaffById(@PathVariable UUID staffId) {
        log.info("Fetching staff details for ID: {}", staffId);
        
        StaffDetailResponse staff = staffService.getStaffById(staffId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Staff details retrieved successfully", staff)
        );
    }

    /**
     * UC_STAFF_03: Add New Staff
     * POST /admin/staffs
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StaffDetailResponse>> addStaff(
            @Valid @RequestBody AddStaffRequest request) {
        log.info("Adding new staff with username: {}", request.getUsername());
        
        StaffDetailResponse staff = staffService.addStaff(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Staff account created successfully. Welcome email has been sent.", staff)
        );
    }

    /**
     * UC_STAFF_04: Edit Staff
     * PUT /admin/staffs/{staffId}
     */
    @PutMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffDetailResponse>> updateStaff(
            @PathVariable UUID staffId,
            @Valid @RequestBody UpdateStaffRequest request) {
        log.info("Updating staff with ID: {}", staffId);
        
        StaffDetailResponse staff = staffService.updateStaff(staffId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Staff information updated successfully", staff)
        );
    }

    /**
     * UC_STAFF_05: Lock/Unlock Staff (Soft Delete)
     * PATCH /admin/staffs/{staffId}/toggle-lock
     */
    @PatchMapping("/{staffId}/toggle-lock")
    public ResponseEntity<ApiResponse<Void>> toggleLockStaff(@PathVariable UUID staffId) {
        log.info("Toggling lock status for staff: {}", staffId);
        
        staffService.toggleLockStaff(staffId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Staff account lock status updated successfully")
        );
    }

    /**
     * UC_STAFF_05: Delete Staff Permanently (Hard Delete)
     * DELETE /admin/staffs/{staffId}
     * 
     * Warning: This is a permanent action and cannot be undone!
     * Will fail if staff has pending/confirmed bookings.
     */
    @DeleteMapping("/{staffId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaffPermanently(@PathVariable UUID staffId) {
        log.info("Attempting to permanently delete staff: {}", staffId);
        
        staffService.deleteStaffPermanently(staffId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Staff account deleted permanently")
        );
    }
}
