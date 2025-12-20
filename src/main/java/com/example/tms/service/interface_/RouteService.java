package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.route.CreateRouteRequest;
import com.example.tms.dto.request.route.RouteFilterRequest;
import com.example.tms.dto.request.route.UpdateRouteRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.route.RouteDetailResponse;
import com.example.tms.dto.response.route.RouteFullDetailResponse;
import com.example.tms.dto.response.route.RouteResponse;

public interface RouteService {
    
    RouteResponse create(CreateRouteRequest request);
    
    RouteResponse getById(UUID id);
    
    RouteDetailResponse getDetailById(UUID id);
    
    /**
     * Get full route detail including trips and itinerary
     */
    RouteFullDetailResponse getFullDetailById(UUID id);

    /**
     * Get images associated with a route
     */
    List<String> getRouteImages(UUID id);
    
    PaginationResponse<RouteResponse> getAll(RouteFilterRequest filter);
    
    List<RouteResponse> getAllNoPagination();
    
    RouteResponse update(UUID id, UpdateRouteRequest request);
    
    void delete(UUID id);
}

