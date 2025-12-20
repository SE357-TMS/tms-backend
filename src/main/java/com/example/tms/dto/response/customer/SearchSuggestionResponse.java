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
public class SearchSuggestionResponse {
    private List<RouteSuggestion> routes;
    private List<AttractionSuggestion> attractions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSuggestion {
        private UUID id;
        private String name;
        private String startLocation;
        private String endLocation;
        private Integer durationDays;
        private String image;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttractionSuggestion {
        private UUID id;
        private String name;
        private String location;
        private String categoryName;
    }
}

