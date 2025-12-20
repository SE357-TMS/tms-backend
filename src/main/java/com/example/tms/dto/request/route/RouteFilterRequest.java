package com.example.tms.dto.request.route;

import com.example.tms.entity.Route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteFilterRequest {
    private String routeName;
    private String startLocation;
    private String endLocation;
    private Route.Status status;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}

