package com.example.tms.dto.request;

import lombok.Data;

@Data
public class UserFilterRequest {
    
    // Search & Filter
    private String keyword;  // Search by username, email, fullName
    private String role;     // Filter by role (CUSTOMER, STAFF, ADMIN)
    private Boolean isLock;  // Filter by lock status
    private String gender;   // Filter by gender (M, F, O)
    
    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    // Admin options
    private Boolean includeDeleted = false;  // Include soft-deleted users
}

