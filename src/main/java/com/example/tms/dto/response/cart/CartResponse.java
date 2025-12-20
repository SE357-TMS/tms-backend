package com.example.tms.dto.response.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {
    private UUID id;
    private UUID userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
}

