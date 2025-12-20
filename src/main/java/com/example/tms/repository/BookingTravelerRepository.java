package com.example.tms.repository;

import com.example.tms.entity.BookingTraveler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingTravelerRepository extends JpaRepository<BookingTraveler, UUID> {
    
    // Find all travelers by booking ID
    @Query("SELECT bt FROM BookingTraveler bt WHERE bt.tourBooking.id = :bookingId AND bt.deletedAt = 0")
    List<BookingTraveler> findByBookingId(@Param("bookingId") UUID bookingId);
    
    // Find traveler by ID and booking ID
    @Query("SELECT bt FROM BookingTraveler bt WHERE bt.id = :id AND bt.tourBooking.id = :bookingId AND bt.deletedAt = 0")
    Optional<BookingTraveler> findByIdAndBookingId(@Param("id") UUID id, @Param("bookingId") UUID bookingId);
}


