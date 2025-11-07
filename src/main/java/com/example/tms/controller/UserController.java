package com.example.tms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.CreateUserRequest;
import com.example.tms.dto.request.UpdateUserRequest;
import com.example.tms.dto.request.UserFilterRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.UserResponse;
import com.example.tms.service.interface_.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    /**
     * Get all users with pagination and filtering
     * ✅ REFACTORED: Use DTO instead of multiple @RequestParam
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAllUsers(
            @ModelAttribute UserFilterRequest filter) {
        PaginationResponse<UserResponse> response = userService.getAllUsers(filter);
        
        if (response.getItems().isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.success("No users found matching the criteria", response)
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Users retrieved successfully", response)
        );
    }

    /**
     * Get all users without pagination (for backward compatibility)
     * ⚠️ DEPRECATED: Use GET /admin/users with pagination instead
     */
    @Deprecated
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersNoPagination() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Get users by role with pagination
     * ✅ REFACTORED: Simplified by using filter DTO
     * Note: Can also use main endpoint with ?role=STAFF filter
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getUsersByRole(
            @PathVariable String role,
            @ModelAttribute UserFilterRequest filter) {
        // Override role in filter
        filter.setRole(role);
        
        PaginationResponse<UserResponse> response = userService.getAllUsers(filter);
        
        return ResponseEntity.ok(
            ApiResponse.success("Users retrieved successfully", response)
        );
    }
}
