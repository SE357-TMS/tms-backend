package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.route.CreateRouteRequest;
import com.example.tms.dto.request.route.RouteFilterRequest;
import com.example.tms.dto.request.route.UpdateRouteRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.route.RouteDetailResponse;
import com.example.tms.dto.response.route.RouteResponse;

public interface RouteService {
    
    RouteResponse create(CreateRouteRequest request);
    
    RouteResponse getById(UUID id);
    
    RouteDetailResponse getDetailById(UUID id);
    
    PaginationResponse<RouteResponse> getAll(RouteFilterRequest filter);
    
    List<RouteResponse> getAllNoPagination();
    
    RouteResponse update(UUID id, UpdateRouteRequest request);
    
    void delete(UUID id);
}
