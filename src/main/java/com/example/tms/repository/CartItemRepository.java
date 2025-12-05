package com.example.tms.repository;

import com.example.tms.enity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    // Find all cart items by cart ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.deletedAt = 0")
    List<CartItem> findByCartId(@Param("cartId") UUID cartId);
    
    // Find cart item by ID and cart ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :id AND ci.cart.id = :cartId AND ci.deletedAt = 0")
    Optional<CartItem> findByIdAndCartId(@Param("id") UUID id, @Param("cartId") UUID cartId);
    
    // Delete all cart items by cart ID (soft delete)
    @Modifying
    @Query("UPDATE CartItem ci SET ci.deletedAt = :timestamp WHERE ci.cart.id = :cartId AND ci.deletedAt = 0")
    int softDeleteByCartId(@Param("cartId") UUID cartId, @Param("timestamp") long timestamp);
}

