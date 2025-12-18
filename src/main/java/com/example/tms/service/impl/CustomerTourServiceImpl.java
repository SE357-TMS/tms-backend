package com.example.tms.service.impl;

import com.example.tms.dto.request.customer.TourSearchRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.customer.HomePageDataResponse;
import com.example.tms.dto.response.customer.SearchSuggestionResponse;
import com.example.tms.dto.response.customer.TourCardResponse;
import com.example.tms.enity.*;
import com.example.tms.repository.*;
import com.example.tms.service.interface_.CustomerTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerTourServiceImpl implements CustomerTourService {

    private final RouteRepository routeRepository;
    private final AttractionRepository attractionRepository;
    private final TripRepository tripRepository;
    private final FavoriteTourRepository favoriteTourRepository;
    private final UserRepository userRepository;

    @Override
    public SearchSuggestionResponse getSearchSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchSuggestionResponse.builder()
                    .routes(Collections.emptyList())
                    .attractions(Collections.emptyList())
                    .build();
        }

        Pageable pageable = PageRequest.of(0, limit);
        
        // Search routes
        List<Route> routes = routeRepository.searchByName(keyword.trim(), pageable);
        List<SearchSuggestionResponse.RouteSuggestion> routeSuggestions = routes.stream()
                .map(r -> SearchSuggestionResponse.RouteSuggestion.builder()
                        .id(r.getId())
                        .name(r.getRouteName())
                        .startLocation(r.getStartLocation())
                        .endLocation(r.getEndLocation())
                        .durationDays(r.getDurationDays())
                        .image(r.getImage())
                        .build())
                .collect(Collectors.toList());

        // Search attractions
        List<Attraction> attractions = attractionRepository.searchByName(keyword.trim(), pageable);
        List<SearchSuggestionResponse.AttractionSuggestion> attractionSuggestions = attractions.stream()
                .map(a -> SearchSuggestionResponse.AttractionSuggestion.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .location(a.getLocation())
                        .categoryName(a.getCategory() != null ? a.getCategory().getName() : null)
                        .build())
                .collect(Collectors.toList());

        return SearchSuggestionResponse.builder()
                .routes(routeSuggestions)
                .attractions(attractionSuggestions)
                .build();
    }

    @Override
    public HomePageDataResponse getHomePageData(UUID userId, int tourLimit, int destinationLimit) {
        // Get most favorited tours
        Pageable tourPageable = PageRequest.of(0, tourLimit);
        List<Object[]> favoritedRoutesData = routeRepository.findMostFavoritedRoutes(tourPageable);
        
        List<TourCardResponse> favoriteTours = favoritedRoutesData.stream()
                .map(row -> {
                    Route route = (Route) row[0];
                    Long favCount = (Long) row[1];
                    return buildTourCard(route, favCount, userId);
                })
                .collect(Collectors.toList());

        // Get favorite destinations (attractions from most favorited routes)
        Pageable destPageable = PageRequest.of(0, destinationLimit);
        List<Object[]> attractionsData = attractionRepository.findFavoriteDestinations(destPageable);
        
        List<HomePageDataResponse.FavoriteDestination> favoriteDestinations = attractionsData.stream()
                .map(row -> {
                    Attraction attraction = (Attraction) row[0];
                    Long tourCount = (Long) row[1];
                    return HomePageDataResponse.FavoriteDestination.builder()
                            .attractionId(attraction.getId())
                            .name(attraction.getName())
                            .location(attraction.getLocation())
                            .image(attraction.getDescription()) // Using description as image placeholder
                            .tourCount(tourCount)
                            .build();
                })
                .collect(Collectors.toList());

        return HomePageDataResponse.builder()
                .favoriteTours(favoriteTours)
                .favoriteDestinations(favoriteDestinations)
                .build();
    }

    @Override
    public PaginationResponse<TourCardResponse> searchTours(TourSearchRequest request, UUID userId) {
        // Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Route> routePage;
        
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            routePage = routeRepository.searchByKeyword(request.getKeyword().trim(), pageable);
        } else {
            // If no keyword, get all open routes
            routePage = routeRepository.findAll((root, query, cb) -> 
                cb.and(
                    cb.equal(root.get("deletedAt"), 0L),
                    cb.equal(root.get("status"), Route.Status.OPEN)
                ), pageable);
        }

        // Filter by additional criteria in memory (for complex filters)
        List<TourCardResponse> tourCards = routePage.getContent().stream()
                .map(route -> {
                    Long favCount = favoriteTourRepository.countByRouteId(route.getId());
                    return buildTourCard(route, favCount, userId);
                })
                .filter(card -> filterTourCard(card, request))
                .collect(Collectors.toList());

        return PaginationResponse.<TourCardResponse>builder()
                .content(tourCards)
                .totalElements(routePage.getTotalElements())
                .totalPages(routePage.getTotalPages())
                .currentPage(routePage.getNumber())
                .pageSize(routePage.getSize())
                .build();
    }

    @Override
    public List<String> getStartLocations() {
        return routeRepository.findDistinctStartLocations();
    }

    @Override
    @Transactional
    public boolean toggleFavorite(UUID userId, UUID routeId) {
        Optional<FavoriteTour> existing = favoriteTourRepository.findByUserIdAndRouteId(userId, routeId);
        
        if (existing.isPresent()) {
            // Remove favorite (soft delete)
            FavoriteTour fav = existing.get();
            fav.setDeletedAt(System.currentTimeMillis());
            favoriteTourRepository.save(fav);
            return false; // Now not favorited
        } else {
            // Add favorite
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            
            FavoriteTour newFav = new FavoriteTour();
            newFav.setUser(user);
            newFav.setRoute(route);
            favoriteTourRepository.save(newFav);
            return true; // Now favorited
        }
    }

    @Override
    public boolean isFavorited(UUID userId, UUID routeId) {
        if (userId == null) return false;
        return favoriteTourRepository.existsByUserIdAndRouteId(userId, routeId);
    }

    // Helper methods
    private TourCardResponse buildTourCard(Route route, Long favCount, UUID userId) {
        // Get upcoming trips for this route
        LocalDate today = LocalDate.now();
        List<Trip> upcomingTrips = tripRepository.findAvailableTripsByRouteId(route.getId(), today);
        
        List<TourCardResponse.TripInfo> tripInfos = upcomingTrips.stream()
                .limit(5) // Only show next 5 trips
                .map(trip -> TourCardResponse.TripInfo.builder()
                        .tripId(trip.getId())
                        .departureDate(trip.getDepartureDate())
                        .returnDate(trip.getReturnDate())
                        .price(trip.getPrice())
                        .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                        .build())
                .collect(Collectors.toList());

        // Get minimum price from upcoming trips
        BigDecimal minPrice = upcomingTrips.stream()
                .map(Trip::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

        // Check if user has favorited this route
        boolean isFav = userId != null && favoriteTourRepository.existsByUserIdAndRouteId(userId, route.getId());

        // Generate route code from first letters
        String routeCode = generateRouteCode(route);

        return TourCardResponse.builder()
                .routeId(route.getId())
                .routeName(route.getRouteName())
                .routeCode(routeCode)
                .startLocation(route.getStartLocation())
                .endLocation(route.getEndLocation())
                .durationDays(route.getDurationDays())
                .image(route.getImage())
                .minPrice(minPrice)
                .favoriteCount(favCount != null ? favCount : 0L)
                .isFavorited(isFav)
                .upcomingTrips(tripInfos)
                .build();
    }

    private String generateRouteCode(Route route) {
        if (route.getRouteName() == null) return "";
        String[] parts = route.getRouteName().split("\\s*-\\s*");
        StringBuilder code = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                code.append(part.charAt(0));
            }
        }
        return code.toString().toUpperCase();
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "default") {
            case "price":
                return Sort.by(direction, "routeName"); // Will sort by minPrice in memory
            case "departuredate":
                return Sort.by(direction, "routeName"); // Will sort by departure date in memory
            case "favoritecount":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private boolean filterTourCard(TourCardResponse card, TourSearchRequest request) {
        // Filter by start location
        if (request.getStartLocation() != null && !request.getStartLocation().isEmpty()) {
            if (!card.getStartLocation().toLowerCase().contains(request.getStartLocation().toLowerCase())) {
                return false;
            }
        }

        // Filter by destination (end location)
        if (request.getDestination() != null && !request.getDestination().isEmpty()) {
            if (!card.getEndLocation().toLowerCase().contains(request.getDestination().toLowerCase())) {
                return false;
            }
        }

        // Filter by duration
        if (request.getDurationDays() != null && card.getDurationDays() != null) {
            if (!card.getDurationDays().equals(request.getDurationDays())) {
                return false;
            }
        }

        // Filter by price range
        if (card.getMinPrice() != null) {
            if (request.getMinPrice() != null && card.getMinPrice().compareTo(request.getMinPrice()) < 0) {
                return false;
            }
            if (request.getMaxPrice() != null && card.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
                return false;
            }
        }

        // Filter by departure date - check if any upcoming trip matches
        if (request.getDepartureDate() != null && card.getUpcomingTrips() != null) {
            boolean hasMatchingTrip = card.getUpcomingTrips().stream()
                    .anyMatch(trip -> !trip.getDepartureDate().isBefore(request.getDepartureDate()));
            if (!hasMatchingTrip && !card.getUpcomingTrips().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
