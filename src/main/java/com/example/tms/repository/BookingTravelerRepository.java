package com.example.tms.repository;

import com.example.tms.enity.BookingTraveler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BookingTravelerRepository extends JpaRepository<BookingTraveler, UUID> {
}

