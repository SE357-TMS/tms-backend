package com.example.tms.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.attraction.AttractionFilterRequest;
import com.example.tms.dto.request.attraction.CreateAttractionRequest;
import com.example.tms.dto.request.attraction.UpdateAttractionRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.attraction.AttractionResponse;
import com.example.tms.enity.Attraction;
import com.example.tms.enity.Category;
import com.example.tms.repository.AttractionRepository;
import com.example.tms.repository.CategoryRepository;
import com.example.tms.service.interface_.AttractionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public AttractionResponse create(CreateAttractionRequest request) {
        Attraction attraction = new Attraction();
        attraction.setName(request.getName());
        attraction.setDescription(request.getDescription());
        attraction.setLocation(request.getLocation());
        attraction.setStatus(Attraction.Status.ACTIVE);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            attraction.setCategory(category);
        }

        Attraction saved = attractionRepository.save(attraction);
        return new AttractionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttractionResponse getById(UUID id) {
        Attraction attraction = attractionRepository.findById(id)
                .filter(a -> a.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Attraction not found"));
        return new AttractionResponse(attraction);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AttractionResponse> getAll(AttractionFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<Attraction> spec = buildSpecification(filter);
        Page<Attraction> page = attractionRepository.findAll(spec, pageable);

        List<AttractionResponse> items = page.getContent().stream()
                .map(AttractionResponse::new)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttractionResponse> getAllNoPagination() {
        return attractionRepository.findAll().stream()
                .filter(a -> a.getDeletedAt() == 0)
                .map(AttractionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttractionResponse update(UUID id, UpdateAttractionRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .filter(a -> a.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Attraction not found"));

        if (request.getName() != null) {
            attraction.setName(request.getName());
        }
        if (request.getDescription() != null) {
            attraction.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            attraction.setLocation(request.getLocation());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            attraction.setCategory(category);
        }
        if (request.getStatus() != null) {
            attraction.setStatus(request.getStatus());
        }

        Attraction updated = attractionRepository.save(attraction);
        return new AttractionResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Attraction attraction = attractionRepository.findById(id)
                .filter(a -> a.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Attraction not found"));
        attraction.markAsDeleted();
        attractionRepository.save(attraction);
    }

    private Specification<Attraction> buildSpecification(AttractionFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Only get non-deleted records
            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }
            if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + filter.getLocation().toLowerCase() + "%"
                ));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
