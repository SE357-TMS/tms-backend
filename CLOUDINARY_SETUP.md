# Cloudinary Image Upload Integration

## ✅ Đã hoàn thành

### 1. **Cấu hình Dependencies**

- ✅ Thêm `cloudinary-http45:1.38.0` vào `pom.xml`
- ✅ Build thành công

### 2. **Cấu hình Cloudinary**

- ✅ **Cloud Name**: `dan4nktek`
- ✅ **API Key**: `783117682814478`
- ✅ **API Secret**: Đã lưu trong `.env`
- ✅ **Folder**: `tms/users` (trên Cloudinary)

### 3. **Naming Convention**

Ảnh được lưu với public_id:

- **Avatar**: `user_{userId}`
  - Ví dụ: `user_123e4567-e89b-12d3-a456-426614174000`
- **Multiple Images**: `user_{userId}_{index}` (index: 1-10)
  - Ví dụ: `user_123e4567-e89b-12d3-a456-426614174000_1`

### 4. **Security & Authorization**

| Action        | CUSTOMER | STAFF    | ADMIN    | PUBLIC |
| ------------- | -------- | -------- | -------- | ------ |
| Upload Avatar | ✅ (own) | ✅ (all) | ✅ (all) | ❌     |
| Upload Image  | ✅ (own) | ✅ (all) | ✅ (all) | ❌     |
| View Avatar   | ✅       | ✅       | ✅       | ✅     |
| Delete Avatar | ✅ (own) | ✅ (all) | ✅ (all) | ❌     |
| Delete Image  | ✅ (own) | ✅ (all) | ✅ (all) | ❌     |

**Security Features:**

- CUSTOMER chỉ có quyền upload/delete ảnh của chính mình
- STAFF/ADMIN có quyền quản lý ảnh của mọi user
- GET avatar là **public** (không cần authentication) để hiển thị trong UI
- File validation: chỉ chấp nhận image, max 10MB

### 5. **API Endpoints**

```
POST   /api/v1/images/users/{userId}/avatar          - Upload avatar
POST   /api/v1/images/users/{userId}/images/{index}  - Upload image (index: 1-10)
GET    /api/v1/images/users/{userId}/avatar          - Get avatar URL (PUBLIC)
DELETE /api/v1/images/users/{userId}/avatar          - Delete avatar
DELETE /api/v1/images/users/{userId}/images/{index}  - Delete image
```

### 6. **Files Created**

```
src/main/java/com/example/tms/
├── config/
│   └── CloudinaryConfig.java                    ✅ Bean configuration
├── service/
│   ├── interface_/
│   │   └── CloudinaryService.java               ✅ Service interface
│   └── impl/
│       └── CloudinaryServiceImpl.java           ✅ Service implementation
├── controller/
│   └── ImageController.java                     ✅ REST endpoints
└── dto/
    └── response/
        └── ImageUploadResponse.java             ✅ Response DTO

src/main/resources/
├── .env                                          ✅ Cloudinary credentials
└── application.properties                       ✅ Config mapping

IMAGE_API_DOCS.md                                 ✅ API documentation
```

---

## 🚀 Test APIs

### 1. Upload Avatar (CUSTOMER - own avatar)

**Request:**

```bash
curl -X POST "http://localhost:8081/api/v1/images/users/{YOUR_USER_ID}/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@avatar.jpg"
```

**Response:**

```json
{
  "success": true,
  "message": "Avatar uploaded successfully",
  "data": {
    "imageUrl": "https://res.cloudinary.com/dan4nktek/image/upload/v1730518800/tms/users/user_{userId}.jpg",
    "message": "Avatar uploaded successfully"
  },
  "timestamp": "2025-11-02T10:30:00"
}
```

### 2. Upload Multiple Images

**Request:**

```bash
curl -X POST "http://localhost:8081/api/v1/images/users/{YOUR_USER_ID}/images/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@image1.jpg"
```

### 3. Get Avatar (PUBLIC - no token needed)

**Request:**

```bash
curl -X GET "http://localhost:8081/api/v1/images/users/{USER_ID}/avatar"
```

### 4. Delete Avatar

**Request:**

```bash
curl -X DELETE "http://localhost:8081/api/v1/images/users/{YOUR_USER_ID}/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 Features

### ✅ Implemented

- [x] Upload user avatar với naming `user_{userId}`
- [x] Upload multiple images với naming `user_{userId}_{index}`
- [x] Get avatar URL (public endpoint)
- [x] Delete avatar/images
- [x] Authorization: CUSTOMER (own), STAFF/ADMIN (all)
- [x] File validation (type, size)
- [x] Overwrite existing images
- [x] HTTPS URLs
- [x] Folder organization: `tms/users/`
- [x] Error handling
- [x] ApiResponse wrapper

### 🔄 How It Works

1. **Upload**: File → Cloudinary → Returns secure_url
2. **Naming**: `user_{userId}` or `user_{userId}_{index}`
3. **Storage**: Cloudinary folder `tms/users/`
4. **Overwrite**: Same publicId = replace old image
5. **Permission Check**:
   - Extract username from JWT
   - Compare with target userId
   - ADMIN/STAFF bypass check

### 📦 Không cần Entity thay đổi

✅ **Không cần thêm field vào User entity**

- Ảnh được identify qua `userId` và `index`
- URL được generate/retrieve từ Cloudinary API
- Không lưu URL trong database

### 🔒 Security Notes

1. **Credentials**:

   - Lưu trong `.env` (local)
   - Nên dùng environment variables khi deploy
   - **Không commit `.env`** lên Git

2. **Authorization**:

   - JWT token required (trừ GET avatar)
   - Permission check trong controller
   - `@PreAuthorize` annotation

3. **Validation**:
   - File type: chỉ image
   - File size: max 10MB
   - Index range: 1-10

---

## 🎯 Next Steps (Tương lai)

Khi cần mở rộng cho entities khác:

```java
// Attraction images
public String uploadAttractionImage(MultipartFile file, UUID attractionId, int index) {
    return uploadImage(file, "attraction_" + attractionId + "_" + index);
}

// Trip images
public String uploadTripImage(MultipartFile file, UUID tripId, int index) {
    return uploadImage(file, "trip_" + tripId + "_" + index);
}

// Tour images
public String uploadTourImage(MultipartFile file, UUID tourId, int index) {
    return uploadImage(file, "tour_" + tourId + "_" + index);
}
```

Chỉ cần:

1. Thêm method vào `CloudinaryService`
2. Implement trong `CloudinaryServiceImpl`
3. Tạo endpoint trong controller tương ứng
4. Set folder khác nếu cần: `tms/attractions/`, `tms/trips/`, etc.

---

## 📚 References

- [IMAGE_API_DOCS.md](./IMAGE_API_DOCS.md) - Chi tiết API documentation
- [Cloudinary Docs](https://cloudinary.com/documentation/java_integration)
- Cloudinary Dashboard: https://console.cloudinary.com/

---

**Status**: ✅ Ready to use! Build successful!
