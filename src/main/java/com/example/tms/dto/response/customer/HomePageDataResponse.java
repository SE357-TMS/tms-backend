package com.example.tms.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageDataResponse {
    private List<TourCardResponse> favoriteTours;
    private List<FavoriteDestination> favoriteDestinations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteDestination {
        private UUID attractionId;
        private String name;
        private String location;
        private String image;
        private Long tourCount;
    }
}
