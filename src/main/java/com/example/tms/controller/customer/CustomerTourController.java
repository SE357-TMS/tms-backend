package com.example.tms.controller.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.customer.TourSearchRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.customer.HomePageDataResponse;
import com.example.tms.dto.response.customer.SearchSuggestionResponse;
import com.example.tms.dto.response.customer.TourCardResponse;
import com.example.tms.service.interface_.CustomerTourService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customer/tours")
@RequiredArgsConstructor
@Tag(name = "Customer Tour APIs", description = "Public APIs for customer tour browsing")
public class CustomerTourController {

    private final CustomerTourService customerTourService;

    @Operation(summary = "Get search suggestions", description = "Get autocomplete suggestions for routes and attractions based on keyword")
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<SearchSuggestionResponse>> getSearchSuggestions(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Maximum results per category") @RequestParam(defaultValue = "5") int limit) {
        SearchSuggestionResponse response = customerTourService.getSearchSuggestions(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success("Suggestions retrieved successfully", response));
    }

    @Operation(summary = "Get home page data", description = "Get favorite tours and destinations for home page")
    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomePageDataResponse>> getHomePageData(
            @Parameter(description = "Number of tours to show") @RequestParam(defaultValue = "5") int tourLimit,
            @Parameter(description = "Number of destinations to show") @RequestParam(defaultValue = "5") int destinationLimit) {
        UUID userId = getCurrentUserId();
        HomePageDataResponse response = customerTourService.getHomePageData(userId, tourLimit, destinationLimit);
        return ResponseEntity.ok(ApiResponse.success("Home page data retrieved successfully", response));
    }

    @Operation(summary = "Search tours", description = "Search and filter tours with pagination")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<TourCardResponse>>> searchTours(
            @ModelAttribute TourSearchRequest request) {
        UUID userId = getCurrentUserId();
        PaginationResponse<TourCardResponse> response = customerTourService.searchTours(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Tours retrieved successfully", response));
    }

    @Operation(summary = "Get all start locations", description = "Get distinct start locations for filter dropdown")
    @GetMapping("/start-locations")
    public ResponseEntity<ApiResponse<List<String>>> getStartLocations() {
        List<String> locations = customerTourService.getStartLocations();
        return ResponseEntity.ok(ApiResponse.success("Start locations retrieved successfully", locations));
    }

    @Operation(summary = "Toggle favorite", description = "Add or remove route from favorites")
    @PostMapping("/{routeId}/favorite")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @Parameter(description = "Route ID") @PathVariable UUID routeId) {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Please login to add favorites"));
        }
        boolean isFavorited = customerTourService.toggleFavorite(userId, routeId);
        String message = isFavorited ? "Route added to favorites" : "Route removed from favorites";
        return ResponseEntity.ok(ApiResponse.success(message, isFavorited));
    }

    @Operation(summary = "Check if favorited", description = "Check if current user has favorited a route")
    @GetMapping("/{routeId}/favorite")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @Parameter(description = "Route ID") @PathVariable UUID routeId) {
        UUID userId = getCurrentUserId();
        boolean isFavorited = customerTourService.isFavorited(userId, routeId);
        return ResponseEntity.ok(ApiResponse.success("Favorite status retrieved", isFavorited));
    }

    // Helper to get current user ID (null if not authenticated)
    private UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                // Assuming principal contains user ID or can be parsed
                String principal = auth.getName();
                return UUID.fromString(principal);
            }
        } catch (Exception e) {
            // Ignore - user not authenticated
        }
        return null;
    }
}
