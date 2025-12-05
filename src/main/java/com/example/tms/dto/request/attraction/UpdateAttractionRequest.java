package com.example.tms.dto.request.attraction;

import java.util.UUID;

import com.example.tms.enity.Attraction;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAttractionRequest {
    
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;
    
    private String description;
    
    @Size(max = 100, message = "Location must be at most 100 characters")
    private String location;
    
    private UUID categoryId;
    
    private Attraction.Status status;
}
