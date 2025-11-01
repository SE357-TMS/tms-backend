package com.example.tms.service.interface_;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.tms.dto.request.CreateUserRequest;
import com.example.tms.dto.request.UpdateUserRequest;
import com.example.tms.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
    Page<UserResponse> getAllUsersWithPagination(Pageable pageable, boolean includeDeleted);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    List<UserResponse> getUsersByRole(String role);
    Page<UserResponse> getUsersByRoleWithPagination(String role, Pageable pageable);
}
