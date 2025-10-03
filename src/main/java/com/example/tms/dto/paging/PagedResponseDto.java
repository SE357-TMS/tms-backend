package com.example.tms.dto.paging;

import lombok.Data;
import java.util.List;

@Data
public class PagedResponseDto<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sortBy;
    private String sortDirection;
}

