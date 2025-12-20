package com.example.tms.dto.response.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetailResponse {
    
    // Personal Information
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
    
    // Work Statistics
    private StaffStatisticsResponse statistics;
    
    // Recent Bookings Handled
    private List<RecentBookingResponse> recentBookings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentBookingResponse {
        private UUID bookingId;
        private String routeName;
        private LocalDateTime bookingDate;
        private String status;
        private Double totalPrice;
        private Integer numberOfTravelers;
    }
}

