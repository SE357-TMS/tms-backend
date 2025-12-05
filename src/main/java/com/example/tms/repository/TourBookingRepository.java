package com.example.tms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.TourBooking;

public interface TourBookingRepository extends JpaRepository<TourBooking, UUID>, JpaSpecificationExecutor<TourBooking> {
    
    // Count bookings by user
    @Query("SELECT COUNT(tb) FROM TourBooking tb WHERE tb.user.id = :userId AND tb.deletedAt = 0")
    Long countByUserId(@Param("userId") UUID userId);
    
    // Count bookings by user and status
    @Query("SELECT COUNT(tb) FROM TourBooking tb WHERE tb.user.id = :userId AND tb.status IN :statuses AND tb.deletedAt = 0")
    Long countByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<TourBooking.Status> statuses);
    
    // Find booking by ID and user ID (for ownership validation)
    @Query("SELECT tb FROM TourBooking tb WHERE tb.id = :bookingId AND tb.user.id = :userId AND tb.deletedAt = 0")
    Optional<TourBooking> findByIdAndUserId(@Param("bookingId") UUID bookingId, @Param("userId") UUID userId);
    
    // Find all bookings by user with pagination
    @Query("SELECT tb FROM TourBooking tb WHERE tb.user.id = :userId AND tb.deletedAt = 0")
    Page<TourBooking> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    // Find all bookings by trip
    @Query("SELECT tb FROM TourBooking tb WHERE tb.trip.id = :tripId AND tb.deletedAt = 0")
    List<TourBooking> findByTripId(@Param("tripId") UUID tripId);
}

