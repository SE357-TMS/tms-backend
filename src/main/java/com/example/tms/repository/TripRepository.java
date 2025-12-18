package com.example.tms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.Trip;

import jakarta.persistence.LockModeType;

public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
    
    // Find trip with pessimistic lock (for booking to avoid race condition)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id AND t.deletedAt = 0")
    Optional<Trip> findByIdWithLock(@Param("id") UUID id);
    
    // Find trip by ID (not deleted)
    @Query("SELECT t FROM Trip t WHERE t.id = :id AND t.deletedAt = 0")
    Optional<Trip> findActiveById(@Param("id") UUID id);
    
    // Find trips by route ID with departure date >= minDate and status = SCHEDULED
    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND t.departureDate >= :minDate AND t.status = 'SCHEDULED' AND t.deletedAt = 0 ORDER BY t.departureDate ASC")
    List<Trip> findAvailableTripsByRouteId(@Param("routeId") UUID routeId, @Param("minDate") LocalDate minDate);
    
    // Find nearest available trip by route ID
    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND t.departureDate >= :minDate AND t.status = 'SCHEDULED' AND t.deletedAt = 0 AND (t.totalSeats - t.bookedSeats) > 0 ORDER BY t.departureDate ASC LIMIT 1")
    Optional<Trip> findNearestAvailableTrip(@Param("routeId") UUID routeId, @Param("minDate") LocalDate minDate);
}

