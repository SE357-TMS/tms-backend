package com.example.tms.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.paging.PagedResponseDto;
import com.example.tms.dto.request.staff.AddStaffRequest;
import com.example.tms.dto.request.staff.StaffFilterRequest;
import com.example.tms.dto.request.staff.UpdateStaffRequest;
import com.example.tms.dto.response.staff.StaffDetailResponse;
import com.example.tms.dto.response.staff.StaffListResponse;
import com.example.tms.enity.User;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.StaffService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<StaffListResponse> getAllStaffs(StaffFilterRequest filter) {
        Specification<User> spec = buildStaffSpecification(filter);
        
        Sort sort = Sort.by(
            filter.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
            filter.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<User> staffPage = userRepository.findAll(spec, pageable);
        
        List<StaffListResponse> staffList = staffPage.getContent().stream()
            .map(this::mapToStaffListResponse)
            .toList();
        
        return PagedResponseDto.of(staffList, staffPage);
    }

    private Specification<User> buildStaffSpecification(StaffFilterRequest filter) {
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("role"), User.Role.STAFF);
        
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("username")), keyword),
                cb.like(cb.lower(root.get("fullName")), keyword),
                cb.like(cb.lower(root.get("email")), keyword)
            ));
        }
        
        if (filter.getPhoneNumber() != null && !filter.getPhoneNumber().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("phoneNumber"), "%" + filter.getPhoneNumber() + "%"));
        }
        
        if (filter.getIsLock() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isLock"), filter.getIsLock()));
        }
        
        if (filter.getGender() != null && !filter.getGender().trim().isEmpty()) {
            try {
                User.Gender gender = User.Gender.valueOf(filter.getGender().toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("gender"), gender));
            } catch (IllegalArgumentException e) {
                // Invalid gender, ignore filter
            }
        }
        
        return spec;
    }

    private StaffListResponse mapToStaffListResponse(User staff) {
        return StaffListResponse.builder()
            .id(staff.getId())
            .username(staff.getUsername())
            .fullName(staff.getFullName())
            .email(staff.getEmail())
            .phoneNumber(staff.getPhoneNumber())
            .address(staff.getAddress())
            .birthday(staff.getBirthday())
            .gender(staff.getGender() != null ? staff.getGender().name() : null)
            .isLock(staff.getIsLock())
            .createdAt(staff.getCreatedAt())
            .updatedAt(staff.getUpdatedAt())
            .totalManagedBookings(0L)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDetailResponse getStaffById(UUID id) {
        User staff = userRepository.findById(id)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
        
        return StaffDetailResponse.builder()
            .id(staff.getId())
            .username(staff.getUsername())
            .fullName(staff.getFullName())
            .email(staff.getEmail())
            .phoneNumber(staff.getPhoneNumber())
            .address(staff.getAddress())
            .birthday(staff.getBirthday())
            .gender(staff.getGender() != null ? staff.getGender().name() : null)
            .isLock(staff.getIsLock())
            .createdAt(staff.getCreatedAt())
            .updatedAt(staff.getUpdatedAt())
            .statistics(null)
            .recentBookings(null)
            .build();
    }

    @Override
    @Transactional
    public StaffDetailResponse createStaff(AddStaffRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        User staff = new User();
        staff.setUsername(request.getUsername());
        staff.setUserPassword(passwordEncoder.encode(request.getPassword()));
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setAddress(request.getAddress());
        staff.setBirthday(request.getBirthday());
        staff.setGender(request.getGender() != null ? User.Gender.valueOf(request.getGender()) : null);
        staff.setRole(User.Role.STAFF);
        staff.setIsLock(false);
        
        User savedStaff = userRepository.save(staff);
        return getStaffById(savedStaff.getId());
    }

    @Override
    @Transactional
    public StaffDetailResponse updateStaff(UUID id, UpdateStaffRequest request) {
        User staff = userRepository.findById(id)
            .filter(u -> u.getRole() == User.Role.STAFF)
            .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
        
        if (request.getEmail() != null) {
            userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new RuntimeException("Email already exists");
                });
            staff.setEmail(request.getEmail());
        }
        
        if (request.getFullName() != null) {
            staff.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            staff.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            staff.setAddress(request.getAddress());
        }
        if (request.getBirthday() != null) {
            staff.setBirthday(request.getBirthday());
        }
        if (request.getGender() != null) {
            staff.setGender(User.Gender.valueOf(request.getGender()));
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            staff.setUserPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User updatedStaff = userRepository.save(staff);
        return getStaffById(updatedStaff.getId());
    }

    @Override
    @Transactional
    public void deleteStaff(UUID id) {
        try {
            User staff = userRepository.findById(id)
                .filter(u -> u.getRole() == User.Role.STAFF)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));

            // Soft delete: set deleted_at timestamp
            staff.setDeletedAt(System.currentTimeMillis());
            userRepository.save(staff);

            // Force flush so unique constraints depending on deleted_at are updated immediately
            userRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Staff was updated by another transaction. Please refresh and try again.");
        }
    }
}
