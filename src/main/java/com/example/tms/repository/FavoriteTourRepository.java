package com.example.tms.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.FavoriteTour;

public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, UUID> {
    
    // Check if user has favorited a route
    @Query("SELECT f FROM FavoriteTour f WHERE f.user.id = :userId AND f.route.id = :routeId AND f.deletedAt = 0")
    Optional<FavoriteTour> findByUserIdAndRouteId(@Param("userId") UUID userId, @Param("routeId") UUID routeId);
    
    // Count favorites for a route
    @Query("SELECT COUNT(f) FROM FavoriteTour f WHERE f.route.id = :routeId AND f.deletedAt = 0")
    Long countByRouteId(@Param("routeId") UUID routeId);
    
    // Check if route is favorited by user
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FavoriteTour f " +
           "WHERE f.user.id = :userId AND f.route.id = :routeId AND f.deletedAt = 0")
    boolean existsByUserIdAndRouteId(@Param("userId") UUID userId, @Param("routeId") UUID routeId);
}

