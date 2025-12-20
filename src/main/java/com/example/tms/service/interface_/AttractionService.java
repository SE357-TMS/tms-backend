package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import com.example.tms.dto.request.attraction.AttractionFilterRequest;
import com.example.tms.dto.request.attraction.CreateAttractionRequest;
import com.example.tms.dto.request.attraction.UpdateAttractionRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.attraction.AttractionResponse;

public interface AttractionService {
    
    AttractionResponse create(CreateAttractionRequest request);
    
    AttractionResponse getById(UUID id);
    
    PaginationResponse<AttractionResponse> getAll(AttractionFilterRequest filter);
    
    List<AttractionResponse> getAllNoPagination();
    
    AttractionResponse update(UUID id, UpdateAttractionRequest request);
    
    void delete(UUID id);
}

