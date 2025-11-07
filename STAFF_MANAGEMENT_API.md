# Staff Management API Documentation

## Overview

Complete CRUD APIs for Staff Management following Use Case specifications UC_STAFF_01 through UC_STAFF_05.

## Base URL

```
http://localhost:8081/admin/staffs
```

## Authentication

All endpoints require:

- **Authentication**: Bearer Token (JWT)
- **Authorization**: `ROLE_ADMIN` only

---

## API Endpoints

### 1. UC_STAFF_01: View and Filter Staffs

**GET** `/admin/staffs`

Retrieve paginated list of staff members with optional filters.

#### Query Parameters

| Parameter     | Type    | Required | Default | Description                             |
| ------------- | ------- | -------- | ------- | --------------------------------------- |
| keyword       | string  | No       | -       | Search by username, full_name, or email |
| phoneNumber   | string  | No       | -       | Filter by phone number                  |
| isLock        | boolean | No       | -       | Filter by lock status                   |
| gender        | string  | No       | -       | Filter by gender (M/F/O)                |
| page          | integer | No       | 0       | Page number (0-indexed)                 |
| size          | integer | No       | 10      | Page size                               |
| sortBy        | string  | No       | id      | Sort field                              |
| sortDirection | string  | No       | DESC    | Sort direction (ASC/DESC)               |

#### Example Request

```bash
GET /admin/staffs?keyword=john&isLock=false&page=0&size=10&sortBy=createdAt&sortDirection=DESC
Authorization: Bearer <your_jwt_token>
```

#### Example Response

```json
{
  "success": true,
  "message": "Staff list retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "john_staff",
        "fullName": "John Doe",
        "email": "john@example.com",
        "phoneNumber": "0901234567",
        "address": "123 Main St, HCMC",
        "birthday": "1990-05-15",
        "gender": "M",
        "isLock": false,
        "createdAt": "2025-01-01T10:00:00",
        "updatedAt": "2025-01-01T10:00:00",
        "totalManagedBookings": 15
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "sortBy": "createdAt",
    "sortDirection": "DESC"
  },
  "timestamp": "2025-11-06T..."
}
```

---

### 2. UC_STAFF_02: View Staff Details

**GET** `/admin/staffs/{staffId}`

Get detailed information about a specific staff member including statistics and recent bookings.

#### Path Parameters

| Parameter | Type | Required | Description     |
| --------- | ---- | -------- | --------------- |
| staffId   | UUID | Yes      | Staff member ID |

#### Example Request

```bash
GET /admin/staffs/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <your_jwt_token>
```

#### Example Response

```json
{
  "success": true,
  "message": "Staff details retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_staff",
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "0901234567",
    "address": "123 Main St, HCMC",
    "birthday": "1990-05-15",
    "gender": "M",
    "isLock": false,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00",
    "statistics": {
      "totalBookingsHandled": 15,
      "totalTripsCreated": 5,
      "totalRoutesCreated": 3
    },
    "recentBookings": [
      {
        "bookingId": "660e8400-e29b-41d4-a716-446655440001",
        "routeName": "Hanoi - Halong Bay",
        "bookingDate": "2025-11-05T14:30:00",
        "status": "CONFIRMED",
        "totalPrice": 5000000.0,
        "numberOfTravelers": 2
      }
    ]
  },
  "timestamp": "2025-11-06T..."
}
```

---

### 3. UC_STAFF_03: Add New Staff

**POST** `/admin/staffs`

Create a new staff account. Automatically creates a Cart and sends welcome email.

#### Request Body

```json
{
  "username": "jane_staff",
  "password": "StrongPass123",
  "fullName": "Jane Smith",
  "email": "jane@example.com",
  "phoneNumber": "0912345678",
  "address": "456 Oak Ave, HCMC",
  "birthday": "1995-08-20",
  "gender": "F"
}
```

#### Validation Rules

| Field       | Rules                                                                  |
| ----------- | ---------------------------------------------------------------------- |
| username    | Required, 3-50 characters, unique                                      |
| password    | Required, ≥8 characters, must contain uppercase, lowercase, and number |
| fullName    | Required, ≤100 characters                                              |
| email       | Required, valid email format, unique                                   |
| phoneNumber | Optional, 10-11 digits                                                 |
| address     | Optional, ≤255 characters                                              |
| birthday    | Required, must be ≥18 years old                                        |
| gender      | Optional, M/F/O                                                        |

#### Example Response

```json
{
  "success": true,
  "message": "Staff account created successfully. Welcome email has been sent.",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "username": "jane_staff",
    "fullName": "Jane Smith",
    "email": "jane@example.com",
    "phoneNumber": "0912345678",
    "address": "456 Oak Ave, HCMC",
    "birthday": "1995-08-20",
    "gender": "F",
    "isLock": false,
    "createdAt": "2025-11-06T11:30:00",
    "updatedAt": "2025-11-06T11:30:00",
    "statistics": {
      "totalBookingsHandled": 0,
      "totalTripsCreated": 0,
      "totalRoutesCreated": 0
    },
    "recentBookings": []
  },
  "timestamp": "2025-11-06T..."
}
```

#### Error Responses

**Username already exists (400)**

```json
{
  "success": false,
  "message": "Username already exists. Please choose another username",
  "timestamp": "2025-11-06T..."
}
```

**Email already registered (400)**

```json
{
  "success": false,
  "message": "Email is already registered. Please use another email",
  "timestamp": "2025-11-06T..."
}
```

**Age validation failed (400)**

```json
{
  "success": false,
  "message": "Staff must be at least 18 years old",
  "timestamp": "2025-11-06T..."
}
```

---

### 4. UC_STAFF_04: Edit Staff

**PUT** `/admin/staffs/{staffId}`

Update staff information. Username is immutable. Sends notification email if email or password changes.

#### Path Parameters

| Parameter | Type | Required | Description     |
| --------- | ---- | -------- | --------------- |
| staffId   | UUID | Yes      | Staff member ID |

#### Request Body

```json
{
  "password": "NewStrongPass456",
  "fullName": "Jane Smith Updated",
  "email": "jane.updated@example.com",
  "phoneNumber": "0919999999",
  "address": "789 New Street, HCMC",
  "birthday": "1995-08-20",
  "gender": "F"
}
```

**Notes:**

- `username` cannot be changed (immutable)
- `password` is optional - only update if provided
- All other fields follow same validation as Add Staff

#### Example Response

```json
{
  "success": true,
  "message": "Staff information updated successfully",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "username": "jane_staff",
    "fullName": "Jane Smith Updated",
    "email": "jane.updated@example.com",
    "phoneNumber": "0919999999",
    "address": "789 New Street, HCMC",
    "birthday": "1995-08-20",
    "gender": "F",
    "isLock": false,
    "createdAt": "2025-11-06T11:30:00",
    "updatedAt": "2025-11-06T15:45:00",
    "statistics": {...},
    "recentBookings": [...]
  },
  "timestamp": "2025-11-06T..."
}
```

#### Error Responses

**Staff not found (404)**

```json
{
  "success": false,
  "message": "Staff not found or has been deleted",
  "timestamp": "2025-11-06T..."
}
```

**Email already used by another account (400)**

```json
{
  "success": false,
  "message": "Email is already registered by another account",
  "timestamp": "2025-11-06T..."
}
```

---

### 5. UC_STAFF_05a: Lock/Unlock Staff (Soft Delete)

**PATCH** `/admin/staffs/{staffId}/toggle-lock`

Toggle staff account lock status (soft delete). Locked accounts cannot login but data is preserved.

#### Path Parameters

| Parameter | Type | Required | Description     |
| --------- | ---- | -------- | --------------- |
| staffId   | UUID | Yes      | Staff member ID |

#### Example Request

```bash
PATCH /admin/staffs/550e8400-e29b-41d4-a716-446655440000/toggle-lock
Authorization: Bearer <your_jwt_token>
```

#### Example Response

```json
{
  "success": true,
  "message": "Staff account lock status updated successfully",
  "timestamp": "2025-11-06T..."
}
```

**Use Cases:**

- **Lock**: Temporarily disable staff access (e.g., leave of absence, investigation)
- **Unlock**: Re-enable staff access
- Preserves all historical data and relationships

---

### 6. UC_STAFF_05b: Delete Staff Permanently (Hard Delete)

**DELETE** `/admin/staffs/{staffId}`

Permanently delete staff account and all related data. **This action cannot be undone!**

#### Path Parameters

| Parameter | Type | Required | Description     |
| --------- | ---- | -------- | --------------- |
| staffId   | UUID | Yes      | Staff member ID |

#### Business Rules

✅ **Allowed:** Staff with no pending/confirmed bookings

❌ **Blocked:** Staff with active bookings (PENDING/CONFIRMED status)

#### Cascade Deletion

Automatically deletes:

1. Cart_Item (all items in staff's cart)
2. Cart (staff's shopping cart)
3. Favorite_Tour (staff's favorite tours)
4. User (staff account)

#### Example Request

```bash
DELETE /admin/staffs/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <your_jwt_token>
```

#### Example Success Response

```json
{
  "success": true,
  "message": "Staff account deleted permanently",
  "timestamp": "2025-11-06T..."
}
```

#### Error Response - Has Pending Bookings (400)

```json
{
  "success": false,
  "message": "Cannot delete staff with 3 pending/confirmed bookings. Please reassign the work or lock the account instead.",
  "timestamp": "2025-11-06T..."
}
```

---

## Error Codes

| Status Code | Description                              |
| ----------- | ---------------------------------------- |
| 200         | Success                                  |
| 201         | Created (Add Staff)                      |
| 400         | Bad Request (Validation failed)          |
| 401         | Unauthorized (No token or invalid token) |
| 403         | Forbidden (Not ADMIN role)               |
| 404         | Not Found (Staff doesn't exist)          |
| 500         | Internal Server Error                    |

---

## Testing with Postman/cURL

### Example: Add Staff

```bash
curl -X POST http://localhost:8081/admin/staffs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_staff",
    "password": "TestPass123",
    "fullName": "Test Staff",
    "email": "test@example.com",
    "phoneNumber": "0901234567",
    "address": "Test Address",
    "birthday": "1990-01-01",
    "gender": "M"
  }'
```

### Example: Filter Staffs

```bash
curl -X GET "http://localhost:8081/admin/staffs?keyword=test&isLock=false&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Example: Toggle Lock

```bash
curl -X PATCH http://localhost:8081/admin/staffs/550e8400-e29b-41d4-a716-446655440000/toggle-lock \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Example: Delete Permanently

```bash
curl -X DELETE http://localhost:8081/admin/staffs/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Database Schema Reference

### User Table (Staffs)

```sql
CREATE TABLE user (
  id UUID PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  user_password VARCHAR(255) NOT NULL,
  is_lock BOOLEAN NOT NULL DEFAULT FALSE,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  address VARCHAR(255),
  birthday DATE,
  gender ENUM('M','F','O'),
  role ENUM('CUSTOMER','STAFF','ADMIN') NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  deleted_at BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL,
  CONSTRAINT uk_username_deleted UNIQUE (username, deleted_at),
  CONSTRAINT uk_email_deleted UNIQUE (email, deleted_at)
);
```

---

## Notes

### Soft Delete vs Hard Delete

**Soft Delete (Lock/Unlock):**

- ✅ Recommended for most cases
- ✅ Preserves historical data
- ✅ Reversible
- ✅ Maintains referential integrity
- Use `is_lock = true`

**Hard Delete (Permanent):**

- ⚠️ Use with caution
- ⚠️ Cannot be undone
- ⚠️ Only allowed if no pending/confirmed bookings
- Automatically cascades to related entities

### Security Considerations

1. **Admin Only**: All endpoints require ADMIN role
2. **Password Hashing**: Passwords are bcrypt-hashed before storage
3. **Email Notifications**: Sent on account creation and sensitive changes
4. **Audit Trail**: All actions are logged
5. **Data Validation**: Strict validation on all inputs

### Future Enhancements

- [ ] Add staff work assignment tracking
- [ ] Implement staff performance metrics
- [ ] Add bulk operations (bulk lock/unlock, bulk delete)
- [ ] Export staff list to CSV/Excel
- [ ] Staff activity audit log viewer
- [ ] Email template customization for welcome emails

---

**Documentation Version**: 1.0  
**Last Updated**: November 6, 2025  
**Author**: TMS Backend Team
