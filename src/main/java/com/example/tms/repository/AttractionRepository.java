package com.example.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.Attraction;

public interface AttractionRepository extends JpaRepository<Attraction, UUID>, JpaSpecificationExecutor<Attraction> {
    
    // Search attractions by name (case-insensitive, contains)
    @Query("SELECT a FROM Attraction a LEFT JOIN FETCH a.category " +
           "WHERE a.deletedAt = 0 AND a.status = 'ACTIVE' " +
           "AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Attraction> searchByName(@Param("keyword") String keyword, Pageable pageable);
    
    // Find attractions from most favorited routes
    @Query("SELECT DISTINCT a, COUNT(DISTINCT f.id) as favCount FROM Attraction a " +
           "JOIN RouteAttraction ra ON ra.attraction.id = a.id AND ra.deletedAt = 0 " +
           "JOIN Route r ON r.id = ra.route.id AND r.deletedAt = 0 AND r.status = 'OPEN' " +
           "LEFT JOIN FavoriteTour f ON f.route.id = r.id AND f.deletedAt = 0 " +
           "WHERE a.deletedAt = 0 AND a.status = 'ACTIVE' " +
           "GROUP BY a ORDER BY favCount DESC")
    List<Object[]> findFavoriteDestinations(Pageable pageable);
}

