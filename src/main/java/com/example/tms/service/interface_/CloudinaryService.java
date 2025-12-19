package com.example.tms.service.interface_;

import java.util.List;
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
    
    /**
     * Upload main route image (updates Route entity's image field)
     * Naming convention: route_{routeId}_main
     * @param file Image file to upload
     * @param routeId Route ID
     * @return URL of uploaded image
     */
    String uploadRouteMainImage(MultipartFile file, UUID routeId);
    
    /**
     * Upload route image with index
     * Naming convention: route_{routeId}_{index}
     * @param file Image file to upload
     * @param routeId Route ID
     * @param index Image index (1, 2, 3, ...)
     * @return URL of uploaded image
     */
    String uploadRouteImage(MultipartFile file, UUID routeId, int index);
    
    /**
     * Delete route image by index
     * @param routeId Route ID
     * @param index Image index
     */
    void deleteRouteImage(UUID routeId, int index);
    
    /**
     * Get route image URL by index (if exists)
     * @param routeId Route ID
     * @param index Image index
     * @return Image URL or null if not found
     */
    String getRouteImageUrl(UUID routeId, int index);
    
    /**
     * Get all route images (up to 10)
     * @param routeId Route ID
     * @return List of image URLs
     */
    List<String> getRouteImages(UUID routeId);
    
    /**
     * Upload main attraction image (uploads to Cloudinary only, does NOT update database)
     * Naming convention: attraction_{attractionId}_main
     * @param file Image file to upload
     * @param attractionId Attraction ID
     * @return URL of uploaded image
     */
    String uploadAttractionMainImage(MultipartFile file, UUID attractionId);
    
    /**
     * Upload attraction image with index
     * Naming convention: attraction_{attractionId}_{index}
     * @param file Image file to upload
     * @param attractionId Attraction ID
     * @param index Image index (1, 2, 3, ...)
     * @return URL of uploaded image
     */
    String uploadAttractionImage(MultipartFile file, UUID attractionId, int index);
    
    /**
     * Delete attraction image by index
     * @param attractionId Attraction ID
     * @param index Image index
     */
    void deleteAttractionImage(UUID attractionId, int index);
    
    /**
     * Get attraction image URL by index (if exists)
     * @param attractionId Attraction ID
     * @param index Image index
     * @return Image URL or null if not found
     */
    String getAttractionImageUrl(UUID attractionId, int index);
    
    /**
     * Get all attraction images (up to 10)
     * @param attractionId Attraction ID
     * @return List of image URLs
     */
    List<String> getAttractionImages(UUID attractionId);
}
