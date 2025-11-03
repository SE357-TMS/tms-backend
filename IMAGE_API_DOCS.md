# Image Upload API Documentation

## Overview

API để quản lý ảnh của User sử dụng Cloudinary. Ảnh được lưu với naming convention:

- Avatar: `user_{userId}`
- Images với index: `user_{userId}_{index}` (1-10)

## Cloudinary Configuration

- **Cloud Name**: dan4nktek
- **Folder**: tms/users
- **Max File Size**: 10MB

## API Endpoints

### 1. Upload User Avatar

**POST** `/api/v1/images/users/{userId}/avatar`

**Authorization:**

- CUSTOMER: Chỉ upload avatar của chính mình
- STAFF/ADMIN: Upload avatar của bất kỳ user nào

**Request:**

- Content-Type: `multipart/form-data`
- Body: `file` (image file)

**Response:**

```json
{
  "success": true,
  "message": "Avatar uploaded successfully",
  "data": {
    "imageUrl": "https://res.cloudinary.com/.../user_123e4567-e89b-12d3-a456-426614174000.jpg",
    "message": "Avatar uploaded successfully"
  },
  "timestamp": "2025-11-02T10:30:00"
}
```

**Example (cURL):**

```bash
curl -X POST "http://localhost:8081/api/v1/images/users/{userId}/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

---

### 2. Upload User Image (Multiple Images)

**POST** `/api/v1/images/users/{userId}/images/{index}`

**Authorization:**

- CUSTOMER: Chỉ upload image của chính mình
- STAFF/ADMIN: Upload image của bất kỳ user nào

**Parameters:**

- `userId`: UUID của user
- `index`: Số thứ tự ảnh (1-10)

**Request:**

- Content-Type: `multipart/form-data`
- Body: `file` (image file)

**Response:**

```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "imageUrl": "https://res.cloudinary.com/.../user_123e4567-e89b-12d3-a456-426614174000_1.jpg",
    "message": "Image 1 uploaded successfully"
  },
  "timestamp": "2025-11-02T10:30:00"
}
```

---

### 3. Get User Avatar

**GET** `/api/v1/images/users/{userId}/avatar`

**Authorization:** PUBLIC (Không cần token)

**Response:**

```json
{
  "success": true,
  "message": "Avatar retrieved successfully",
  "data": {
    "imageUrl": "https://res.cloudinary.com/.../user_123e4567-e89b-12d3-a456-426614174000.jpg",
    "message": "Avatar retrieved successfully"
  },
  "timestamp": "2025-11-02T10:30:00"
}
```

**Response (No avatar):**

```json
{
  "success": true,
  "message": "User has no avatar",
  "data": null,
  "timestamp": "2025-11-02T10:30:00"
}
```

---

### 4. Delete User Avatar

**DELETE** `/api/v1/images/users/{userId}/avatar`

**Authorization:**

- CUSTOMER: Chỉ xóa avatar của chính mình
- STAFF/ADMIN: Xóa avatar của bất kỳ user nào

**Response:**

```json
{
  "success": true,
  "message": "Avatar deleted successfully",
  "data": null,
  "timestamp": "2025-11-02T10:30:00"
}
```

---

### 5. Delete User Image

**DELETE** `/api/v1/images/users/{userId}/images/{index}`

**Authorization:**

- CUSTOMER: Chỉ xóa image của chính mình
- STAFF/ADMIN: Xóa image của bất kỳ user nào

**Parameters:**

- `userId`: UUID của user
- `index`: Số thứ tự ảnh (1-10)

**Response:**

```json
{
  "success": true,
  "message": "Image 1 deleted successfully",
  "data": null,
  "timestamp": "2025-11-02T10:30:00"
}
```

---

## Permission Matrix

| Endpoint      | CUSTOMER | STAFF | ADMIN | PUBLIC |
| ------------- | -------- | ----- | ----- | ------ |
| Upload Avatar | Own only | All   | All   | ❌     |
| Upload Image  | Own only | All   | All   | ❌     |
| Get Avatar    | ✅       | ✅    | ✅    | ✅     |
| Delete Avatar | Own only | All   | All   | ❌     |
| Delete Image  | Own only | All   | All   | ❌     |

---

## Error Responses

### 400 Bad Request

```json
{
  "success": false,
  "message": "File cannot be empty",
  "timestamp": "2025-11-02T10:30:00"
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "You don't have permission to modify this user's images",
  "timestamp": "2025-11-02T10:30:00"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "User not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-11-02T10:30:00"
}
```

---

## File Validation

- **Allowed types**: image/jpeg, image/png, image/gif, image/webp
- **Max size**: 10MB
- **Max images per user**: 1 avatar + 10 indexed images

---

## Cloudinary Naming Convention

- **Avatar**: `tms/users/user_{userId}`
- **Image 1**: `tms/users/user_{userId}_1`
- **Image 2**: `tms/users/user_{userId}_2`
- ...
- **Image 10**: `tms/users/user_{userId}_10`

---

## Notes

- Uploading với cùng `publicId` sẽ **overwrite** ảnh cũ (do `overwrite: true`)
- Tất cả URL đều dùng **HTTPS** (`secure: true`)
- Ảnh được lưu trong folder `tms/users` trên Cloudinary
- GET avatar là public để có thể hiển thị trong profile, danh sách users, etc.
