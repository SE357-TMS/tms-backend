package com.example.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.TourBooking;

public interface TourBookingRepository extends JpaRepository<TourBooking, UUID> {
    
    // Count bookings by user
    @Query("SELECT COUNT(tb) FROM TourBooking tb WHERE tb.user.id = :userId")
    Long countByUserId(@Param("userId") UUID userId);
    
    // Count bookings by user and status
    @Query("SELECT COUNT(tb) FROM TourBooking tb WHERE tb.user.id = :userId AND tb.status IN :statuses")
    Long countByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<String> statuses);
}

