package com.example.tms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.cart.AddToCartRequest;
import com.example.tms.dto.request.cart.UpdateCartItemRequest;
import com.example.tms.dto.response.cart.CartItemResponse;
import com.example.tms.dto.response.cart.CartResponse;
import com.example.tms.enity.Cart;
import com.example.tms.enity.CartItem;
import com.example.tms.enity.Trip;
import com.example.tms.enity.User;
import com.example.tms.repository.CartItemRepository;
import com.example.tms.repository.CartRepository;
import com.example.tms.repository.TripRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String username) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String username, AddToCartRequest request) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + request.getTripId()));
        
        // Check available seats
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        if (request.getQuantity() > availableSeats) {
            throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
        }
        
        // Check if trip already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndTripId(cart.getId(), trip.getId())
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > availableSeats) {
                throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setTrip(trip);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(trip.getPrice());
            cartItemRepository.save(newItem);
        }
        
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String username, UUID cartItemId, UpdateCartItemRequest request) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user's cart");
        }
        
        Trip trip = cartItem.getTrip();
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        
        if (request.getQuantity() > availableSeats) {
            throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(String username, UUID cartItemId) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user's cart");
        }
        
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        User user = getUserByUsername(username);
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTripInCart(String username, UUID tripId) {
        User user = getUserByUsername(username);
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return false;
        }
        return cartItemRepository.findByCartIdAndTripId(cart.getId(), tripId).isPresent();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdWithTrip(cart.getId());
        
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        
        List<CartItemResponse> itemResponses = items.stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());
        
        response.setItems(itemResponses);
        
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        response.setTotalAmount(totalAmount);
        response.setTotalItems(items.stream().mapToInt(CartItem::getQuantity).sum());
        
        return response;
    }
}
