package com.example.tms.dto.response.attraction;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.enity.Attraction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttractionResponse {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private UUID categoryId;
    private String categoryName;
    private Attraction.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public AttractionResponse(Attraction attraction) {
        this.id = attraction.getId();
        this.name = attraction.getName();
        this.description = attraction.getDescription();
        this.location = attraction.getLocation();
        if (attraction.getCategory() != null) {
            this.categoryId = attraction.getCategory().getId();
            this.categoryName = attraction.getCategory().getName();
        }
        this.status = attraction.getStatus();
        this.createdAt = attraction.getCreatedAt();
        this.updatedAt = attraction.getUpdatedAt();
    }
}
