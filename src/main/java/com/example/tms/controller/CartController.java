package com.example.tms.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.cart.AddToCartRequest;
import com.example.tms.dto.request.cart.UpdateCartItemRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.cart.CartResponse;
import com.example.tms.service.interface_.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get cart", description = "Get current user's shopping cart")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        CartResponse response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", response));
    }

    @Operation(summary = "Add to cart", description = "Add a trip to the shopping cart")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or not enough seats")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", response));
    }

    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Authentication authentication,
            @Parameter(description = "Cart Item ID") @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(authentication.getName(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", response));
    }

    @Operation(summary = "Remove from cart", description = "Remove an item from the shopping cart")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            Authentication authentication,
            @Parameter(description = "Cart Item ID") @PathVariable UUID cartItemId) {
        cartService.removeFromCart(authentication.getName(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully"));
    }

    @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }

    @Operation(summary = "Check trip in cart", description = "Check if a specific trip is in the user's cart")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Check completed")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @GetMapping("/check/{tripId}")
    public ResponseEntity<ApiResponse<Boolean>> hasTripInCart(
            Authentication authentication,
            @Parameter(description = "Trip ID") @PathVariable UUID tripId) {
        boolean result = cartService.hasTripInCart(authentication.getName(), tripId);
        return ResponseEntity.ok(ApiResponse.success("Check completed", result));
    }
}
