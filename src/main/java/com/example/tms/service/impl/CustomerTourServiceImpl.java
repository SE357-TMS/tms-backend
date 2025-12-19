package com.example.tms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.customer.TourSearchRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.customer.FavoriteDestinationImageResponse;
import com.example.tms.dto.response.customer.HomePageDataResponse;
import com.example.tms.dto.response.customer.SearchSuggestionResponse;
import com.example.tms.dto.response.customer.TourCardResponse;
import com.example.tms.enity.Attraction;
import com.example.tms.enity.FavoriteTour;
import com.example.tms.enity.Route;
import com.example.tms.enity.RouteAttraction;
import com.example.tms.enity.Trip;
import com.example.tms.enity.User;
import com.example.tms.repository.AttractionRepository;
import com.example.tms.repository.FavoriteTourRepository;
import com.example.tms.repository.RouteAttractionRepository;
import com.example.tms.repository.RouteRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.CloudinaryService;
import com.example.tms.service.interface_.CustomerTourService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerTourServiceImpl implements CustomerTourService {

    private final RouteRepository routeRepository;
    private final AttractionRepository attractionRepository;
    private final RouteAttractionRepository routeAttractionRepository;
    private final TripRepository tripRepository;
    private final FavoriteTourRepository favoriteTourRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

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
        List<UUID> favoriteRouteIds = favoritedRoutesData.stream()
                .map(row -> ((Route) row[0]).getId())
                .collect(Collectors.toList());

        final Map<UUID, List<UUID>> favoriteRouteAttractionMap = new HashMap<>();
        if (!favoriteRouteIds.isEmpty()) {
            List<RouteAttraction> favoriteRouteAttractions = routeAttractionRepository.findByRouteIdIn(favoriteRouteIds);
            Map<UUID, List<UUID>> temp = favoriteRouteAttractions.stream()
                    .collect(Collectors.groupingBy(
                        ra -> ra.getRoute().getId(),
                        Collectors.mapping(ra -> ra.getAttraction().getId(), Collectors.toList())
                    ));
            favoriteRouteAttractionMap.putAll(temp);
        }

        List<TourCardResponse> favoriteTours = favoritedRoutesData.stream()
                .map(row -> {
                    Route route = (Route) row[0];
                    Long favCount = (Long) row[1];
                    List<UUID> attractionIds = favoriteRouteAttractionMap.getOrDefault(route.getId(), Collections.emptyList());
                    return buildTourCard(route, favCount, userId, attractionIds);
                })
                .collect(Collectors.toList());

        // Get favorite destinations (attractions from most favorited routes)
        List<Object[]> attractionsData = loadFavoriteDestinations(destinationLimit);
        List<HomePageDataResponse.FavoriteDestination> favoriteDestinations = buildFavoriteDestinations(attractionsData);

        return HomePageDataResponse.builder()
                .favoriteTours(favoriteTours)
                .favoriteDestinations(favoriteDestinations)
                .build();
    }

    @Override
    public List<FavoriteDestinationImageResponse> getFavoriteDestinationImages(int destinationLimit) {
        List<Object[]> attractionsData = loadFavoriteDestinations(destinationLimit);
        List<CompletableFuture<FavoriteDestinationImageResponse>> imageFutures = attractionsData.stream()
                .map(row -> {
                    Attraction attraction = (Attraction) row[0];
                    return CompletableFuture.supplyAsync(() -> {
                        String imageUrl = null;
                        try {
                            imageUrl = cloudinaryService.getAttractionImageUrl(attraction.getId(), 1);
                        } catch (Exception ignored) {
                        }
                        return FavoriteDestinationImageResponse.builder()
                                .attractionId(attraction.getId())
                                .image(imageUrl)
                                .build();
                    });
                })
                .collect(Collectors.toList());

        return imageFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private List<Object[]> loadFavoriteDestinations(int destinationLimit) {
        Pageable destPageable = PageRequest.of(0, destinationLimit);
        return attractionRepository.findFavoriteDestinations(destPageable);
    }

    private List<HomePageDataResponse.FavoriteDestination> buildFavoriteDestinations(List<Object[]> attractionsData) {
        return attractionsData.stream()
                .map(row -> {
                    Attraction attraction = (Attraction) row[0];
                    Long tourCount = (Long) row[1];
                    return HomePageDataResponse.FavoriteDestination.builder()
                            .attractionId(attraction.getId())
                            .name(attraction.getName())
                            .location(attraction.getLocation())
                            .description(attraction.getDescription())
                            .tourCount(tourCount)
                            .image(null)
                            .build();
                })
                .collect(Collectors.toList());
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

        List<UUID> routeIds = routePage.getContent().stream()
                .map(Route::getId)
                .collect(Collectors.toList());

        final Map<UUID, List<UUID>> attractionMap = new HashMap<>();
        if (!routeIds.isEmpty()) {
            List<RouteAttraction> routeAttractions = routeAttractionRepository.findByRouteIdIn(routeIds);
            Map<UUID, List<UUID>> temp = routeAttractions.stream()
                    .collect(Collectors.groupingBy(
                        ra -> ra.getRoute().getId(),
                        Collectors.mapping(ra -> ra.getAttraction().getId(), Collectors.toList())
                    ));
            attractionMap.putAll(temp);
        }

        // Filter by additional criteria in memory (for complex filters)
        List<TourCardResponse> tourCards = routePage.getContent().stream()
                .map(route -> {
                    Long favCount = favoriteTourRepository.countByRouteId(route.getId());
                    List<UUID> attractionIds = attractionMap.getOrDefault(route.getId(), Collections.emptyList());
                    return buildTourCard(route, favCount, userId, attractionIds);
                })
                .filter(card -> filterTourCard(card, request))
                .collect(Collectors.toList());

        return new PaginationResponse<>(routePage, tourCards);
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
    private TourCardResponse buildTourCard(Route route, Long favCount, UUID userId, List<UUID> attractionIds) {
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
        String routeCode = generateRouteCodeFromId(route.getId());

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
                .attractionIds(attractionIds)
                .build();
    }

    private String generateRouteCodeFromId(UUID routeId) {
        if (routeId == null) return "";
        String raw = routeId.toString().replace("-", "").toUpperCase();
        return raw.substring(0, Math.min(8, raw.length()));
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
        if (request.getAttractionId() != null) {
            List<UUID> attractionIds = card.getAttractionIds();
            boolean matchesRouteName = request.getKeyword() != null && !request.getKeyword().trim().isEmpty()
                    && card.getRouteName() != null
                    && card.getRouteName().toLowerCase().contains(request.getKeyword().trim().toLowerCase());
            if ((attractionIds == null || !attractionIds.contains(request.getAttractionId()))
                    && !matchesRouteName) {
                return false;
            }
        }

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
