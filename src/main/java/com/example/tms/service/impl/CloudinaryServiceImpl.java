package com.example.tms.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.tms.service.interface_.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private static final String USER_FOLDER = "tms/users"; // Folder for user images
    private static final String ROUTE_FOLDER = "tms/routes"; // Folder for route images

    @Override
    public String uploadUserAvatar(MultipartFile file, UUID userId) {
        return uploadImage(file, "user_" + userId, USER_FOLDER);
    }

    @Override
    public String uploadUserImage(MultipartFile file, UUID userId, int index) {
        return uploadImage(file, "user_" + userId + "_" + index, USER_FOLDER);
    }

    @Override
    public void deleteUserAvatar(UUID userId) {
        deleteImage("user_" + userId, USER_FOLDER);
    }

    @Override
    public void deleteUserImage(UUID userId, int index) {
        deleteImage("user_" + userId + "_" + index, USER_FOLDER);
    }

    @Override
    public String getUserAvatarUrl(UUID userId) {
        try {
            String publicId = USER_FOLDER + "/user_" + userId;
            Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result.get("secure_url").toString();
        } catch (Exception e) {
            log.warn("User avatar not found for userId: {}", userId);
            return null;
        }
    }

    @Override
    public String getUserImageUrl(UUID userId, int index) {
        try {
            String publicId = USER_FOLDER + "/user_" + userId + "_" + index;
            Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result.get("secure_url").toString();
        } catch (Exception e) {
            log.warn("User image not found for userId: {} at index: {}", userId, index);
            return null;
        }
    }
    
    @Override
    public String uploadRouteImage(MultipartFile file, UUID routeId, int index) {
        return uploadImage(file, "route_" + routeId + "_" + index, ROUTE_FOLDER);
    }
    
    @Override
    public void deleteRouteImage(UUID routeId, int index) {
        deleteImage("route_" + routeId + "_" + index, ROUTE_FOLDER);
    }
    
    @Override
    public String getRouteImageUrl(UUID routeId, int index) {
        try {
            String publicId = ROUTE_FOLDER + "/route_" + routeId + "_" + index;
            Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result.get("secure_url").toString();
        } catch (Exception e) {
            log.debug("Route image not found for routeId: {} at index: {}", routeId, index);
            return null;
        }
    }
    
    @Override
    public List<String> getRouteImages(UUID routeId) {
        List<String> images = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String url = getRouteImageUrl(routeId, i);
            if (url != null) {
                images.add(url);
            }
        }
        return images;
    }

    /**
     * Generic method to upload image to Cloudinary
     * @param file Image file
     * @param publicId Public ID (filename without extension)
     * @param folder Folder path in Cloudinary
     * @return Secure URL of uploaded image
     */
    private String uploadImage(MultipartFile file, String publicId, String folder) {
        validateFile(file);
        
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "overwrite", true, // Overwrite if exists
                "resource_type", "image"
            );
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String secureUrl = uploadResult.get("secure_url").toString();
            
            log.info("Successfully uploaded image: {} -> {}", publicId, secureUrl);
            return secureUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload image: {}", publicId, e);
            throw new RuntimeException("Could not upload image to Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Generic method to delete image from Cloudinary
     * @param publicId Public ID (filename without extension)
     * @param folder Folder path in Cloudinary
     */
    private void deleteImage(String publicId, String folder) {
        try {
            String fullPublicId = folder + "/" + publicId;
            Map result = cloudinary.uploader().destroy(fullPublicId, ObjectUtils.emptyMap());
            
            if ("ok".equals(result.get("result"))) {
                log.info("Successfully deleted image: {}", publicId);
            } else {
                log.warn("Image not found or already deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete image: {}", publicId, e);
            throw new RuntimeException("Could not delete image from Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Max 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }
    }
}
