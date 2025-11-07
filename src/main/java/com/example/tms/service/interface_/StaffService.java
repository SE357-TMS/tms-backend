package com.example.tms.service.interface_;

import java.util.UUID;

import com.example.tms.dto.paging.PagedResponseDto;
import com.example.tms.dto.request.staff.AddStaffRequest;
import com.example.tms.dto.request.staff.StaffFilterRequest;
import com.example.tms.dto.request.staff.UpdateStaffRequest;
import com.example.tms.dto.response.staff.StaffDetailResponse;
import com.example.tms.dto.response.staff.StaffListResponse;

public interface StaffService {
    
    /**
     * UC_STAFF_01: View and Filter Staffs
     * Get paginated list of staffs with optional filters
     */
    PagedResponseDto<StaffListResponse> getAllStaffs(StaffFilterRequest filter);
    
    /**
     * UC_STAFF_02: View Staff Details
     * Get detailed information about a specific staff member
     */
    StaffDetailResponse getStaffById(UUID staffId);
    
    /**
     * UC_STAFF_03: Add New Staff
     * Create a new staff account
     */
    StaffDetailResponse addStaff(AddStaffRequest request);
    
    /**
     * UC_STAFF_04: Edit Staff
     * Update staff information
     */
    StaffDetailResponse updateStaff(UUID staffId, UpdateStaffRequest request);
    
    /**
     * UC_STAFF_05: Delete Staff (Soft delete - Lock account)
     * Lock/Unlock staff account
     */
    void toggleLockStaff(UUID staffId);
    
    /**
     * UC_STAFF_05: Delete Staff (Hard delete - Permanent)
     * Permanently delete staff account (with validation)
     */
    void deleteStaffPermanently(UUID staffId);
}
