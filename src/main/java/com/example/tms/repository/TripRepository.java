package com.example.tms.repository;

import com.example.tms.enity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
    
    // Find trip with pessimistic lock (for booking to avoid race condition)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id AND t.deletedAt = 0")
    Optional<Trip> findByIdWithLock(@Param("id") UUID id);
    
    // Find trip by ID (not deleted)
    @Query("SELECT t FROM Trip t WHERE t.id = :id AND t.deletedAt = 0")
    Optional<Trip> findActiveById(@Param("id") UUID id);
}

