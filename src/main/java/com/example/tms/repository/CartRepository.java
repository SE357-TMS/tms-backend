package com.example.tms.repository;

import com.example.tms.enity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    // Find cart by user ID
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.deletedAt = 0")
    Optional<Cart> findByUserId(@Param("userId") UUID userId);
}

