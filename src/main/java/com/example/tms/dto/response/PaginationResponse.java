package com.example.tms.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationResponse<T> {
    private Integer page;           // Current page (1-indexed)
    private Integer totalPages;     // Total number of pages
    private Integer pageSize;       // Items per page
    private Long totalElements;     // Total number of items
    private List<T> items;          // List of items in current page

    public PaginationResponse(Page<?> pageModel, List<T> items) {
        this.page = pageModel.getNumber() + 1;  // Convert from 0-indexed to 1-indexed
        this.totalPages = pageModel.getTotalPages();
        this.pageSize = pageModel.getSize();
        this.totalElements = pageModel.getTotalElements();
        this.items = items;
    }

    // Constructor with direct values
    public PaginationResponse(List<T> items, Long totalElements, Integer page, Integer pageSize) {
        this.items = items;
        this.totalElements = totalElements;
        this.page = page + 1; // Convert from 0-indexed to 1-indexed
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
}
