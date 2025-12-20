package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.tms.dto.request.CreateUserRequest;
import com.example.tms.dto.request.UpdateUserRequest;
import com.example.tms.dto.request.UserFilterRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    // Admin-only: create a user with ADMIN role
    UserResponse createAdmin(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    
    // New method with filter DTO
    PaginationResponse<UserResponse> getAllUsers(UserFilterRequest filter);
    
    // Legacy methods (keep for backward compatibility)
    List<UserResponse> getAllUsers();
    Page<UserResponse> getAllUsersWithPagination(Pageable pageable, boolean includeDeleted);
    
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    List<UserResponse> getUsersByRole(String role);
    Page<UserResponse> getUsersByRoleWithPagination(String role, Pageable pageable);
}

