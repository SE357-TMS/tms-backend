package com.example.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.Route;

public interface RouteRepository extends JpaRepository<Route, UUID>, JpaSpecificationExecutor<Route> {
    
    // Search routes by name (case-insensitive, contains)
    @Query("SELECT r FROM Route r WHERE r.deletedAt = 0 AND LOWER(r.routeName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Route> searchByName(@Param("keyword") String keyword, Pageable pageable);
    
    // Find routes by start location
    @Query("SELECT r FROM Route r WHERE r.deletedAt = 0 AND LOWER(r.startLocation) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Route> findByStartLocation(@Param("location") String location);
    
    // Find routes with most favorites
    @Query("SELECT r, COUNT(f) as favCount FROM Route r LEFT JOIN FavoriteTour f ON f.route.id = r.id AND f.deletedAt = 0 " +
           "WHERE r.deletedAt = 0 AND r.status = 'OPEN' GROUP BY r ORDER BY favCount DESC")
    List<Object[]> findMostFavoritedRoutes(Pageable pageable);
    
    // Get distinct start locations
    @Query("SELECT DISTINCT r.startLocation FROM Route r WHERE r.deletedAt = 0 AND r.status = 'OPEN'")
    List<String> findDistinctStartLocations();
    
    // Search routes by keyword in name or related attractions
    @Query("SELECT DISTINCT r FROM Route r " +
           "LEFT JOIN RouteAttraction ra ON ra.route.id = r.id AND ra.deletedAt = 0 " +
           "LEFT JOIN Attraction a ON a.id = ra.attraction.id AND a.deletedAt = 0 " +
           "WHERE r.deletedAt = 0 AND r.status = 'OPEN' " +
           "AND (LOWER(r.routeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Route> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

