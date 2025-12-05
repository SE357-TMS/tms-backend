package com.example.tms.dto.request.routeattraction;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteAttractionFilterRequest {
    private UUID routeId;
    private UUID attractionId;
    private Integer day;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "day";
    private String sortDirection = "asc";
}
