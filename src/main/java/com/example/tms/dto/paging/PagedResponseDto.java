package com.example.tms.dto.paging;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PagedResponseDto<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sortBy;
    private String sortDirection;
    
    /**
     * Create PagedResponseDto from Spring Data Page
     */
    public static <T> PagedResponseDto<T> of(List<T> content, Page<?> page) {
        PagedResponseDto<T> response = new PagedResponseDto<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        
        if (page.getSort().isSorted()) {
            page.getSort().forEach(order -> {
                response.setSortBy(order.getProperty());
                response.setSortDirection(order.getDirection().name());
            });
        }
        
        return response;
    }
}


