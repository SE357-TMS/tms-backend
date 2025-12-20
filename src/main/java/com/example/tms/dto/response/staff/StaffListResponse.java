package com.example.tms.dto.response.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffListResponse {
    
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate birthday;
    private String gender;
    private Boolean isLock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics
    private Long totalManagedBookings;
}

