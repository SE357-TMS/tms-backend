package com.example.tms.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCardResponse {
    private UUID routeId;
    private String routeName;
    private String routeCode;
    private String startLocation;
    private String endLocation;
    private Integer durationDays;
    private String image;
    private BigDecimal minPrice;
    private Long favoriteCount;
    private boolean isFavorited;
    private List<TripInfo> upcomingTrips;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripInfo {
        private UUID tripId;
        private LocalDate departureDate;
        private LocalDate returnDate;
        private BigDecimal price;
        private Integer availableSeats;
    }
}
