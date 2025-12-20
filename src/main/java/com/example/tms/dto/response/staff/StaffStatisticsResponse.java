package com.example.tms.dto.response.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffStatisticsResponse {
    
    private Long totalBookingsHandled;
    private Long totalTripsCreated;
    private Long totalRoutesCreated;
}

