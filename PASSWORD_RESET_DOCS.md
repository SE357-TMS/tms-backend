# Password Reset with Email - SendGrid Integration

## ✅ Đã hoàn thành

### 1. **Dependencies**

- ✅ SendGrid Java SDK 4.10.2
- ✅ Build thành công

### 2. **SendGrid Configuration**

- ✅ **API Key**: `SG.8CpuC3r7TBqvE7qphev5-A...`
- ✅ **From Email**: `noreply@tms-tourism.com`
- ✅ **From Name**: `TMS Tourism Management`

### 3. **Database Schema**

```sql
CREATE TABLE password_reset_token (
    id VARCHAR(36) PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

### 4. **Flow Diagram**

```
┌─────────────────────────────────────────────────────────────┐
│                    Password Reset Flow                       │
└─────────────────────────────────────────────────────────────┘

1. User Forgot Password
   │
   ├─→ POST /auth/password/forgot
   │   Body: { "email": "user@example.com" }
   │
   ├─→ Backend validates email
   │   ├─ User exists? ✓
   │   ├─ User not deleted? ✓
   │   └─ User not locked? ✓
   │
   ├─→ Generate unique token (UUID)
   │   └─ Save to database (valid for 15 minutes)
   │
   ├─→ Send email via SendGrid
   │   └─ Email contains reset link with token
   │
   └─→ Response: "Check your email for reset instructions"

2. User Clicks Reset Link
   │
   ├─→ Frontend: GET http://localhost:3000/reset-password?token=xxx
   │
   ├─→ Frontend validates token
   │   └─ GET /auth/password/validate-token?token=xxx
   │
   └─→ Shows password reset form if valid

3. User Submits New Password
   │
   ├─→ POST /auth/password/reset
   │   Body: {
   │     "token": "xxx",
   │     "newPassword": "newPass123"
   │   }
   │
   ├─→ Backend validates token
   │   ├─ Token exists? ✓
   │   ├─ Not expired? ✓
   │   └─ Not used? ✓
   │
   ├─→ Update password (BCrypt)
   │   └─ Mark token as used
   │
   ├─→ Send confirmation email
   │   └─ "Your password has been changed"
   │
   └─→ Response: "Password reset successful"
```

---

## 📧 Email Templates

### **Reset Password Email**

```html
Subject: Reset Your Password - TMS Tourism Content:
┌─────────────────────────────────────────┐ │ 🔐 Reset Your Password │ │ │ │
Hello [User Name], │ │ │ │ Click the button below to reset: │ │ [Reset Password
Button] │ │ │ │ Or copy this link: │ │ http://localhost:3000/reset-password?.. │
│ │ │ ⏰ Expires in 15 minutes │ │ │ │ ⚠️ Didn't request this? Ignore it. │
└─────────────────────────────────────────┘
```

### **Password Changed Confirmation**

```html
Subject: Password Changed Successfully - TMS Tourism Content:
┌─────────────────────────────────────────┐ │ ✅ Password Changed Successfully │
│ │ │ Hello [User Name], │ │ │ │ Your password was changed successfully. │ │ You
can now login with your new pass. │ │ │ │ ⚠️ Didn't make this change? │ │
Contact support immediately. │ └─────────────────────────────────────────┘
```

---

## 🔌 API Endpoints

### **1. Request Password Reset**

```http
POST /auth/password/forgot
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response (Success - 200 OK):**

```json
{
  "success": true,
  "message": "If an account exists with this email, you will receive password reset instructions.",
  "data": "Password reset email sent successfully",
  "timestamp": "2025-11-05T21:00:00"
}
```

**Response (User không tồn tại):**

```json
{
  "success": true,
  "message": "If an account exists with this email, you will receive password reset instructions.",
  "data": "Request processed",
  "timestamp": "2025-11-05T21:00:00"
}
```

**Note**: Vì lý do bảo mật, không tiết lộ email có tồn tại hay không.

---

### **2. Validate Reset Token**

```http
GET /auth/password/validate-token?token=550e8400-e29b-41d4-a716-446655440000
```

**Response (Valid Token):**

```json
{
  "success": true,
  "message": "Token is valid",
  "data": true,
  "timestamp": "2025-11-05T21:05:00"
}
```

**Response (Invalid/Expired Token):**

```json
{
  "success": false,
  "message": "Token is invalid or expired",
  "data": false,
  "timestamp": "2025-11-05T21:05:00"
}
```

---

### **3. Reset Password**

```http
POST /auth/password/reset
Content-Type: application/json

{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewSecurePass123!"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Password has been reset successfully. You can now login with your new password.",
  "data": "Password reset successful",
  "timestamp": "2025-11-05T21:10:00"
}
```

**Response Errors:**

```json
// Token đã được dùng
{
  "success": false,
  "message": "This reset token has already been used",
  "timestamp": "2025-11-05T21:10:00"
}

// Token hết hạn
{
  "success": false,
  "message": "This reset token has expired",
  "timestamp": "2025-11-05T21:10:00"
}

// Token không tồn tại
{
  "success": false,
  "message": "Invalid or expired reset token",
  "timestamp": "2025-11-05T21:10:00"
}
```

---

## 🧪 Testing Guide

### **Test 1: Request Password Reset**

**Postman/cURL:**

```bash
curl -X POST http://localhost:8081/auth/password/forgot \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com"
  }'
```

**Expected:**

1. ✅ Response 200 OK
2. ✅ Email được gửi đến inbox
3. ✅ Token được lưu trong database
4. ✅ Token có thời hạn 15 phút

**Check database:**

```sql
SELECT * FROM password_reset_token
WHERE user_id = (SELECT id FROM user WHERE email = 'admin@example.com')
ORDER BY created_at DESC
LIMIT 1;
```

---

### **Test 2: Check Email**

**Kiểm tra email inbox:**

1. Subject: "Reset Your Password - TMS Tourism"
2. From: "TMS Tourism Management <noreply@tms-tourism.com>"
3. Contains: Reset button và link
4. Link format: `http://localhost:3000/reset-password?token=xxx`

**Lấy token từ email** (hoặc từ database để test)

---

### **Test 3: Validate Token**

**Request:**

```bash
curl -X GET "http://localhost:8081/auth/password/validate-token?token=YOUR_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Token is valid",
  "data": true
}
```

---

### **Test 4: Reset Password**

**Request:**

```bash
curl -X POST http://localhost:8081/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_TOKEN_HERE",
    "newPassword": "NewPassword123!"
  }'
```

**Expected:**

1. ✅ Response 200 OK
2. ✅ Password changed in database
3. ✅ Token marked as `used = true`
4. ✅ Confirmation email sent

**Verify password changed:**

```bash
# Login with new password
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "NewPassword123!"
  }'
```

---

### **Test 5: Token Already Used**

**Request (same token again):**

```bash
curl -X POST http://localhost:8081/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "ALREADY_USED_TOKEN",
    "newPassword": "AnotherPass123"
  }'
```

**Expected Error:**

```json
{
  "success": false,
  "message": "This reset token has already been used"
}
```

---

### **Test 6: Expired Token**

**Wait 15 minutes, then:**

```bash
curl -X GET "http://localhost:8081/auth/password/validate-token?token=EXPIRED_TOKEN"
```

**Expected:**

```json
{
  "success": false,
  "message": "Token is invalid or expired",
  "data": false
}
```

---

## 🔒 Security Features

### ✅ Implemented

- [x] Token expires after 15 minutes
- [x] Token can only be used once
- [x] Old tokens deleted when requesting new one
- [x] Password hashed with BCrypt (strength 12)
- [x] No email disclosure (generic success message)
- [x] Validates user not deleted/locked
- [x] HTTPS for SendGrid
- [x] Token is UUID (cryptographically random)

### 🛡️ Best Practices

1. **Rate Limiting**: Nên thêm rate limiting cho `/forgot` endpoint (tránh spam)
2. **Frontend URL**: Đổi `app.frontend.url` khi deploy production
3. **SendGrid API Key**: Dùng environment variable khi deploy
4. **Token Cleanup**: Có thể thêm scheduled task xóa expired tokens

---

## 📋 Files Created

```
src/main/java/com/example/tms/
├── enity/
│   └── PasswordResetToken.java              ✅ Entity
├── repository/
│   └── PasswordResetTokenRepository.java    ✅ Repository
├── dto/
│   └── request/
│       ├── ForgotPasswordRequest.java       ✅ DTO
│       └── ResetPasswordRequest.java        ✅ DTO
├── service/
│   ├── interface_/
│   │   ├── EmailService.java                ✅ Service interface
│   │   └── PasswordResetService.java        ✅ Service interface
│   └── impl/
│       ├── EmailServiceImpl.java            ✅ SendGrid impl
│       └── PasswordResetServiceImpl.java    ✅ Business logic
└── controller/
    └── PasswordResetController.java         ✅ REST endpoints

pom.xml                                      ✅ SendGrid dependency
.env                                         ✅ API keys
application.properties                       ✅ Config
```

---

## 🚀 Ready to Use!

**Status**: ✅ Build successful, ready for testing!

**Next Steps:**

1. Start server: `.\mvnw.cmd spring-boot:run`
2. Test forgot password with your email
3. Check email inbox
4. Test reset password flow

**Note**: Cần verify email từ SendGrid nếu đây là tài khoản free. Nếu không, email sẽ chỉ gửi được đến email đã verify.
