package com.example.tms.controller.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.customer.CustomerProfileResponse;
import com.example.tms.entity.User;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.CloudinaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customer/profile")
@RequiredArgsConstructor
@Tag(name = "Customer Profile APIs", description = "APIs for customer profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerProfileController {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Get current user profile", description = "Get profile information for the currently authenticated user")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'STAFF')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCurrentProfile(Authentication authentication) {
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String avatarUrl = null;
        try {
            avatarUrl = cloudinaryService.getUserAvatarUrl(user.getId());
        } catch (Exception e) {
            // Ignore - user has no avatar
        }
        
        CustomerProfileResponse response = CustomerProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole().name())
                .avatarUrl(avatarUrl)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }
}

