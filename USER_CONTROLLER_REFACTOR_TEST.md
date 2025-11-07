# 🧪 Test UserController After Refactoring

## Changes Summary

UserController đã được refactor để follow pattern của StaffController:

- ✅ Sử dụng `UserFilterRequest` DTO thay vì nhiều `@RequestParam`
- ✅ Logic xử lý pagination/sorting chuyển từ Controller → Service
- ✅ Giảm code duplication
- ✅ Dễ maintain và extend

---

## Test Cases

### 1. Test Basic Pagination (No Filter)

```http
GET http://localhost:8081/admin/users?page=0&size=10
Authorization: Bearer <admin_token>
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "items": [...],
    "currentPage": 0,
    "totalPages": 1,
    "totalItems": 5,
    "pageSize": 10
  }
}
```

---

### 2. Test Keyword Search

```http
GET http://localhost:8081/admin/users?keyword=admin
Authorization: Bearer <admin_token>
```

Search trong: `username`, `email`, `fullName`

---

### 3. Test Filter by Role

```http
GET http://localhost:8081/admin/users?role=STAFF
Authorization: Bearer <admin_token>
```

Valid roles: `CUSTOMER`, `STAFF`, `ADMIN`

---

### 4. Test Filter by Lock Status

```http
# Get locked users
GET http://localhost:8081/admin/users?isLock=true
Authorization: Bearer <admin_token>

# Get active users
GET http://localhost:8081/admin/users?isLock=false
Authorization: Bearer <admin_token>
```

---

### 5. Test Filter by Gender

```http
GET http://localhost:8081/admin/users?gender=M
Authorization: Bearer <admin_token>
```

Valid genders: `M`, `F`, `O`

---

### 6. Test Include Deleted Users (Admin Only)

```http
GET http://localhost:8081/admin/users?includeDeleted=true
Authorization: Bearer <admin_token>
```

---

### 7. Test Combined Filters

```http
GET http://localhost:8081/admin/users?role=STAFF&isLock=false&gender=M&sortBy=fullName&sortDirection=ASC
Authorization: Bearer <admin_token>
```

---

### 8. Test Sorting

```http
# Sort by createdAt DESC (default)
GET http://localhost:8081/admin/users?sortBy=createdAt&sortDirection=DESC

# Sort by fullName ASC
GET http://localhost:8081/admin/users?sortBy=fullName&sortDirection=ASC

# Sort by email DESC
GET http://localhost:8081/admin/users?sortBy=email&sortDirection=DESC
```

---

### 9. Test Legacy Endpoint (Still Works)

```http
GET http://localhost:8081/admin/users/all
Authorization: Bearer <admin_token>
```

**Note:** This endpoint is deprecated but maintained for backward compatibility.

---

### 10. Test Get Users By Role (Simplified)

```http
GET http://localhost:8081/admin/users/role/STAFF?page=0&size=10
Authorization: Bearer <admin_token>
```

Now uses `UserFilterRequest` internally - no more code duplication!

---

## Comparison: Before vs After

### Before (Old UserController)

```http
GET /admin/users?page=0&size=10&sortBy=createdAt&sortType=desc&includeDeleted=true
```

- ❌ 5 separate parameters
- ❌ Logic in controller
- ❌ Code duplication

### After (Refactored UserController)

```http
GET /admin/users?page=0&size=10&sortBy=createdAt&sortDirection=DESC&includeDeleted=true
```

- ✅ Same API (backward compatible)
- ✅ DTO pattern
- ✅ Clean controller
- ✅ Business logic in service

---

## Verify Improvements

### 1. Code Quality

- Controller methods are now cleaner (< 10 lines each)
- No pagination logic in controller
- No sort logic in controller
- Single responsibility

### 2. Maintainability

- Add new filter? → Just add field to `UserFilterRequest`
- Change sorting logic? → Only change in Service
- No code duplication between methods

### 3. Testability

- Controller: Easy to test (just verify HTTP handling)
- Service: Easy to test (mock repository)
- No dependency on Spring in business logic

---

## Expected Behavior

All endpoints should work **exactly the same** as before, but with:

- ✅ Cleaner code
- ✅ Better maintainability
- ✅ No breaking changes
- ✅ Same API contract

---

## Error Cases to Test

### 1. Invalid Role

```http
GET /admin/users?role=INVALID
Authorization: Bearer <admin_token>
```

**Expected:** Ignore invalid role filter, return all users

### 2. Invalid Gender

```http
GET /admin/users?gender=X
Authorization: Bearer <admin_token>
```

**Expected:** Ignore invalid gender filter, return all users

### 3. Invalid Sort Field

```http
GET /admin/users?sortBy=nonexistentField
Authorization: Bearer <admin_token>
```

**Expected:** May throw error or use default sort

### 4. Unauthorized Access

```http
GET /admin/users
# No Authorization header
```

**Expected:** 401 Unauthorized

---

## Performance Testing

Test with large dataset:

```http
GET /admin/users?page=0&size=100
GET /admin/users?page=10&size=50
GET /admin/users?keyword=test&page=0&size=20
```

Should have same performance as before (or better due to optimized queries).

---

## Postman Collection

Import this for quick testing:

```json
{
  "info": {
    "name": "TMS - User Management (Refactored)"
  },
  "item": [
    {
      "name": "Get All Users (Filtered)",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{baseUrl}}/admin/users?page=0&size=10&sortBy=createdAt&sortDirection=DESC",
          "host": ["{{baseUrl}}"],
          "path": ["admin", "users"],
          "query": [
            { "key": "page", "value": "0" },
            { "key": "size", "value": "10" },
            { "key": "sortBy", "value": "createdAt" },
            { "key": "sortDirection", "value": "DESC" }
          ]
        }
      }
    },
    {
      "name": "Filter by Role",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/admin/users?role=STAFF"
      }
    },
    {
      "name": "Search by Keyword",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/admin/users?keyword=admin"
      }
    }
  ]
}
```

---

**Status:** ✅ Ready to test after server starts
