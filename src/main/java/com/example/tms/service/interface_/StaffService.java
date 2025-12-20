package com.example.tms.service.interface_;

import java.util.UUID;

import com.example.tms.dto.paging.PagedResponseDto;
import com.example.tms.dto.request.staff.AddStaffRequest;
import com.example.tms.dto.request.staff.StaffFilterRequest;
import com.example.tms.dto.request.staff.UpdateStaffRequest;
import com.example.tms.dto.response.staff.StaffDetailResponse;
import com.example.tms.dto.response.staff.StaffListResponse;

public interface StaffService {
    PagedResponseDto<StaffListResponse> getAllStaffs(StaffFilterRequest filter);
    StaffDetailResponse getStaffById(UUID id);
    StaffDetailResponse createStaff(AddStaffRequest request);
    StaffDetailResponse updateStaff(UUID id, UpdateStaffRequest request);
    void deleteStaff(UUID id);
}

