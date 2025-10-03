package com.example.tms.repository;

import com.example.tms.enity.TourBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TourBookingDetailRepository extends JpaRepository<TourBookingDetail, UUID> {
}

