package com.example.tms.dto.request.attraction;

import java.util.UUID;

import com.example.tms.enity.Attraction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttractionFilterRequest {
    private String name;
    private String location;
    private UUID categoryId;
    private Attraction.Status status;
    
    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
