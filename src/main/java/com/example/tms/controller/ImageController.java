package com.example.tms.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.ImageUploadResponse;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    /**/
    /*
     * Upload user avatar
     * - CUSTOMER: Can only upload their own avatar
     * - ADMIN/STAFF: Can upload any user's avatar
     */
    @PostMapping(value = "/users/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'STAFF', 'ADMIN')")
    public ApiResponse<ImageUploadResponse> uploadUserAvatar(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        // Check permission: CUSTOMER can only upload their own avatar
        checkUserPermission(userId, authentication);
        
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        String imageUrl = cloudinaryService.uploadUserAvatar(file, userId);
        ImageUploadResponse response = new ImageUploadResponse(imageUrl, "Avatar uploaded successfully");
        
        return ApiResponse.success("Avatar uploaded successfully", response);
    }

    /**
     * Upload user image with index (for multiple images)
     * - CUSTOMER: Can only upload their own images
     * - ADMIN/STAFF: Can upload any user's images
     */
    @PostMapping(value = "/users/{userId}/images/{index}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'STAFF', 'ADMIN')")
    public ApiResponse<ImageUploadResponse> uploadUserImage(
            @PathVariable UUID userId,
            @PathVariable int index,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        // Check permission
        checkUserPermission(userId, authentication);
        
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        String imageUrl = cloudinaryService.uploadUserImage(file, userId, index);
        ImageUploadResponse response = new ImageUploadResponse(
                imageUrl, 
                "Image " + index + " uploaded successfully"
        );
        
        return ApiResponse.success("Image uploaded successfully", response);
    }

    /**
     * Get user avatar URL
     * - PUBLIC: Anyone can view user avatars
     */
    @GetMapping("/users/{userId}/avatar")
    public ApiResponse<ImageUploadResponse> getUserAvatar(@PathVariable UUID userId) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        String imageUrl = cloudinaryService.getUserAvatarUrl(userId);
        
        if (imageUrl == null) {
            return ApiResponse.success("User has no avatar", null);
        }
        
        ImageUploadResponse response = new ImageUploadResponse(imageUrl, "Avatar retrieved successfully");
        return ApiResponse.success("Avatar retrieved successfully", response);
    }

    /**
     * Get user image URL by index
     * - PUBLIC: Anyone can view user images
     */
    @GetMapping("/users/{userId}/images/{index}")
    public ApiResponse<ImageUploadResponse> getUserImage(
            @PathVariable UUID userId,
            @PathVariable int index) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        String imageUrl = cloudinaryService.getUserImageUrl(userId, index);
        
        if (imageUrl == null) {
            return ApiResponse.success("User has no image at index " + index, null);
        }
        
        ImageUploadResponse response = new ImageUploadResponse(imageUrl, "Image retrieved successfully");
        return ApiResponse.success("Image retrieved successfully", response);
    }

    /**
     * Delete user avatar
     * - CUSTOMER: Can only delete their own avatar
     * - ADMIN/STAFF: Can delete any user's avatar
     */
    @DeleteMapping("/users/{userId}/avatar")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'STAFF', 'ADMIN')")
    public ApiResponse<String> deleteUserAvatar(
            @PathVariable UUID userId,
            Authentication authentication) {
        
        // Check permission
        checkUserPermission(userId, authentication);
        
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        cloudinaryService.deleteUserAvatar(userId);
        return ApiResponse.success("Avatar deleted successfully");
    }

    /**
     * Delete user image by index
     * - CUSTOMER: Can only delete their own images
     * - ADMIN/STAFF: Can delete any user's images
     */
    @DeleteMapping("/users/{userId}/images/{index}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'STAFF', 'ADMIN')")
    public ApiResponse<String> deleteUserImage(
            @PathVariable UUID userId,
            @PathVariable int index,
            Authentication authentication) {
        
        // Check permission
        checkUserPermission(userId, authentication);
        
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        cloudinaryService.deleteUserImage(userId, index);
        return ApiResponse.success("Image " + index + " deleted successfully");
    }
    
    // ========== ROUTE IMAGE ENDPOINTS ==========
    
    /**
     * Upload route image with index
     * - ADMIN/STAFF only
     */
    @PostMapping(value = "/routes/{routeId}/images/{index}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('STAFF', 'ADMIN')")
    public ApiResponse<ImageUploadResponse> uploadRouteImage(
            @PathVariable UUID routeId,
            @PathVariable int index,
            @RequestParam("file") MultipartFile file) {
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        String imageUrl = cloudinaryService.uploadRouteImage(file, routeId, index);
        ImageUploadResponse response = new ImageUploadResponse(
                imageUrl, 
                "Route image " + index + " uploaded successfully"
        );
        
        return ApiResponse.success("Image uploaded successfully", response);
    }
    
    /**
     * Get route image URL by index
     * - PUBLIC: Anyone can view route images
     */
    @GetMapping("/routes/{routeId}/images/{index}")
    public ApiResponse<ImageUploadResponse> getRouteImage(
            @PathVariable UUID routeId,
            @PathVariable int index) {
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        String imageUrl = cloudinaryService.getRouteImageUrl(routeId, index);
        
        if (imageUrl == null) {
            return ApiResponse.success("Route has no image at index " + index, null);
        }
        
        ImageUploadResponse response = new ImageUploadResponse(imageUrl, "Image retrieved successfully");
        return ApiResponse.success("Image retrieved successfully", response);
    }
    
    /**
     * Get all route images
     * - PUBLIC: Anyone can view route images
     */
    @GetMapping("/routes/{routeId}/images")
    public ApiResponse<java.util.List<String>> getRouteImages(@PathVariable UUID routeId) {
        java.util.List<String> images = cloudinaryService.getRouteImages(routeId);
        return ApiResponse.success("Route images retrieved successfully", images);
    }
    
    /**
     * Delete route image by index
     * - ADMIN/STAFF only
     */
    @DeleteMapping("/routes/{routeId}/images/{index}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'ADMIN')")
    public ApiResponse<String> deleteRouteImage(
            @PathVariable UUID routeId,
            @PathVariable int index) {
        
        if (index < 1 || index > 10) {
            throw new IllegalArgumentException("Image index must be between 1 and 10");
        }
        
        cloudinaryService.deleteRouteImage(routeId, index);
        return ApiResponse.success("Route image " + index + " deleted successfully");
    }

    /**
     * Check if user has permission to modify images
     * CUSTOMER can only modify their own images
     * ADMIN/STAFF can modify any user's images
     */
    private void checkUserPermission(UUID userId, Authentication authentication) {
        String username = authentication.getName();
        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // ADMIN and STAFF can modify any user's images
        if (currentUser.getRole().name().equals("ADMIN") || 
            currentUser.getRole().name().equals("STAFF")) {
            return;
        }
        
        // CUSTOMER can only modify their own images
        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to modify this user's images");
        }
    }
}
