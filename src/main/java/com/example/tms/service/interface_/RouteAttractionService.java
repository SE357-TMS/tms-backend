package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.routeattraction.CreateRouteAttractionRequest;
import com.example.tms.dto.request.routeattraction.RouteAttractionFilterRequest;
import com.example.tms.dto.request.routeattraction.UpdateRouteAttractionRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.routeattraction.RouteAttractionResponse;

public interface RouteAttractionService {
    
    RouteAttractionResponse create(CreateRouteAttractionRequest request);
    
    RouteAttractionResponse getById(UUID id);
    
    PaginationResponse<RouteAttractionResponse> getAll(RouteAttractionFilterRequest filter);
    
    List<RouteAttractionResponse> getByRouteId(UUID routeId);
    
    RouteAttractionResponse update(UUID id, UpdateRouteAttractionRequest request);
    
    void delete(UUID id);
}
