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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users and customers")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Create user",
        description = "Create a new user account"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @Operation(
        summary = "Get user by ID",
        description = "Retrieve user details by their unique identifier"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @Operation(
        summary = "Get all users",
        description = "Retrieve a paginated list of users with optional filters"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
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

    @Operation(
        summary = "Get all users (no pagination)",
        description = "Retrieve all users without pagination. Deprecated - use GET /admin/users instead"
    )
    @Deprecated
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersNoPagination() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", response)
        );
    }

    @Operation(
        summary = "Update user",
        description = "Update user information by ID"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @Operation(
        summary = "Delete user",
        description = "Delete a user by ID (soft delete)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @Operation(
        summary = "Get users by role",
        description = "Retrieve users filtered by their role (ADMIN, STAFF, CUSTOMER)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getUsersByRole(
            @Parameter(description = "User role (ADMIN, STAFF, CUSTOMER)") @PathVariable String role,
            @ModelAttribute UserFilterRequest filter) {
        // Override role in filter
        filter.setRole(role);
        
        PaginationResponse<UserResponse> response = userService.getAllUsers(filter);
        
        return ResponseEntity.ok(
            ApiResponse.success("Users retrieved successfully", response)
        );
    }
}
