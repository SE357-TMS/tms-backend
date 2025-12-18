package com.example.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.RouteAttraction;

public interface RouteAttractionRepository extends JpaRepository<RouteAttraction, UUID>, JpaSpecificationExecutor<RouteAttraction> {
    
    @Query("SELECT ra FROM RouteAttraction ra " +
           "JOIN FETCH ra.attraction a " +
           "LEFT JOIN FETCH a.category " +
           "WHERE ra.route.id = :routeId AND ra.deletedAt = 0 " +
           "ORDER BY ra.day ASC, ra.orderInDay ASC")
    List<RouteAttraction> findByRouteIdOrderByDayAndOrder(@Param("routeId") UUID routeId);
    
    @Query("SELECT ra FROM RouteAttraction ra WHERE ra.route.id = :routeId AND ra.deletedAt = 0")
    List<RouteAttraction> findByRouteId(@Param("routeId") UUID routeId);
}

