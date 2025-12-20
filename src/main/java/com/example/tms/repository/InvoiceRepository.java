package com.example.tms.repository;

import com.example.tms.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    
    // Find invoice by booking ID
    @Query("SELECT i FROM Invoice i WHERE i.tourBooking.id = :bookingId AND i.deletedAt = 0")
    Optional<Invoice> findByBookingId(@Param("bookingId") UUID bookingId);
}


