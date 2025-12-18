package com.example.tms.dto.request.customer;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourSearchRequest {
    private String keyword;
    private String startLocation;
    private String destination;
    private LocalDate departureDate;
    private Integer durationDays;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy = "favoriteCount"; // favoriteCount, price, departureDate
    private String sortOrder = "desc"; // asc, desc
    private Integer page = 0;
    private Integer size = 10;
}
