package com.example.tms.service.interface_;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    
    /**
     * Upload user avatar with naming convention: user_{userId}
     * @param file Image file to upload
     * @param userId User ID
     * @return URL of uploaded image
     */
    String uploadUserAvatar(MultipartFile file, UUID userId);
    
    /**
     * Upload user image with index (for multiple images)
     * Naming convention: user_{userId}_{index}
     * @param file Image file to upload
     * @param userId User ID
     * @param index Image index (1, 2, 3, ...)
     * @return URL of uploaded image
     */
    String uploadUserImage(MultipartFile file, UUID userId, int index);
    
    /**
     * Delete user avatar
     * @param userId User ID
     */
    void deleteUserAvatar(UUID userId);
    
    /**
     * Delete user image by index
     * @param userId User ID
     * @param index Image index
     */
    void deleteUserImage(UUID userId, int index);
    
    /**
     * Get user avatar URL (if exists)
     * @param userId User ID
     * @return Image URL or null if not found
     */
    String getUserAvatarUrl(UUID userId);
    
    /**
     * Get user image URL by index (if exists)
     * @param userId User ID
     * @param index Image index
     * @return Image URL or null if not found
     */
    String getUserImageUrl(UUID userId, int index);
}
