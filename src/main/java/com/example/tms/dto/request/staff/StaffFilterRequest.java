package com.example.tms.dto.request.staff;

import lombok.Data;

@Data
public class StaffFilterRequest {
    
    private String keyword; // Search by username, full_name, or email
    private String phoneNumber;
    private Boolean isLock;
    private String gender; // M, F, O
    
    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "DESC";
}
