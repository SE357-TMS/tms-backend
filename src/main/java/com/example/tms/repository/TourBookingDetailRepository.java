package com.example.tms.repository;

import com.example.tms.enity.TourBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TourBookingDetailRepository extends JpaRepository<TourBookingDetail, UUID> {
    
    // Find booking detail by booking ID
    @Query("SELECT tbd FROM TourBookingDetail tbd WHERE tbd.tourBooking.id = :bookingId AND tbd.deletedAt = 0")
    Optional<TourBookingDetail> findByBookingId(@Param("bookingId") UUID bookingId);
}

