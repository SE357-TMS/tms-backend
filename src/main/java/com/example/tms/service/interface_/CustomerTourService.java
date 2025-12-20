package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.customer.TourSearchRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.customer.FavoriteDestinationImageResponse;
import com.example.tms.dto.response.customer.HomePageDataResponse;
import com.example.tms.dto.response.customer.SearchSuggestionResponse;
import com.example.tms.dto.response.customer.TourCardResponse;

public interface CustomerTourService {
    
    /**
     * Get search suggestions (autocomplete) for routes and attractions
     */
    SearchSuggestionResponse getSearchSuggestions(String keyword, int limit);
    
    /**
     * Get home page data (textual fields only) for favorite tours and destinations
     */
    HomePageDataResponse getHomePageData(UUID userId, int tourLimit, int destinationLimit);

    /**
     * Get favorite destination images separately to avoid blocking home data
     */
    List<FavoriteDestinationImageResponse> getFavoriteDestinationImages(int destinationLimit);
    
    /**
     * Search tours with filters and pagination
     */
    PaginationResponse<TourCardResponse> searchTours(TourSearchRequest request, UUID userId);
    
    /**
     * Get all distinct start locations for filter dropdown
     */
    List<String> getStartLocations();
    
    /**
     * Toggle favorite for a route
     */
    boolean toggleFavorite(UUID userId, UUID routeId);
    
    /**
     * Check if route is favorited by user
     */
    boolean isFavorited(UUID userId, UUID routeId);
}

