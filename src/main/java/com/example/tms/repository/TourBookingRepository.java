package com.example.tms.repository;

import com.example.tms.enity.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TourBookingRepository extends JpaRepository<TourBooking, UUID> {
}

