package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.cart.AddToCartRequest;
import com.example.tms.dto.request.cart.UpdateCartItemRequest;
import com.example.tms.dto.response.cart.CartResponse;

public interface CartService {
    CartResponse getCart(String username);
    CartResponse addToCart(String username, AddToCartRequest request);
    CartResponse updateCartItem(String username, UUID cartItemId, UpdateCartItemRequest request);
    void removeFromCart(String username, UUID cartItemId);
    void removeMultipleFromCart(String username, List<UUID> cartItemIds);
    void clearCart(String username);
    boolean hasTripInCart(String username, UUID tripId);
}

