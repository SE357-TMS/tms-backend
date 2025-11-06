# 📧 Email Service Setup Guide

## Overview

This project uses **SendGrid** for sending password reset emails.

## Important Notice About Email Delivery

### ⚠️ Emails May Go to Spam Folder

**This is NORMAL and EXPECTED** for this student project because:

1. **Free SendGrid Tier**: Using free plan without domain authentication
2. **Personal Email as Sender**: Sending from `dangphuthien2005@gmail.com` instead of custom domain
3. **No Domain Verification**: Missing SPF/DKIM records (requires domain ownership)
4. **Low Sender Reputation**: New SendGrid account without email history

### ✅ How to Find Emails

1. **Check your Spam/Junk folder**
2. Look for email with subject: `[TMS Tourism] Password Reset Request`
3. From: `TMS Tourism Management <dangphuthien2005@gmail.com>`
4. **Mark as "Not Spam"** to receive future emails in inbox

### 📧 Email Features

#### Password Reset Email

- **Subject**: `[TMS Tourism] Password Reset Request`
- **Contains**: Reset link valid for 15 minutes
- **Format**: Both HTML and plain text versions

#### Password Changed Confirmation

- **Subject**: `[TMS Tourism] Password Changed Successfully`
- **Sent**: After successful password reset

## Configuration

### Environment Variables (.env)

```properties
SENDGRID_API_KEY=SG.your_api_key_here
SENDGRID_FROM_EMAIL=dangphuthien2005@gmail.com
SENDGRID_FROM_NAME=TMS Tourism Management
```

### Application Properties

```properties
sendgrid.api.key=${SENDGRID_API_KEY}
sendgrid.from.email=${SENDGRID_FROM_EMAIL}
sendgrid.from.name=${SENDGRID_FROM_NAME}
app.frontend.url=http://localhost:3000
```

## Testing Instructions

### 1. Request Password Reset

```bash
POST http://localhost:8081/auth/password/forgot
Content-Type: application/json

{
  "email": "your-email@example.com"
}
```

### 2. Check Email

- ✅ Check **Inbox** first
- ⚠️ If not found, check **Spam/Junk folder**
- 📧 Look for subject: `[TMS Tourism] Password Reset Request`

### 3. Use Reset Link

- Click the button in email OR
- Copy the reset link and open in browser
- Link format: `http://localhost:3000/reset-password?token=...`

### 4. Reset Password

```bash
POST http://localhost:8081/auth/password/reset
Content-Type: application/json

{
  "token": "token-from-email",
  "newPassword": "NewPassword123!"
}
```

## For Production (Future Improvements)

To avoid spam folder in production:

### Option 1: Domain Authentication (Recommended)

1. Register a domain (e.g., `tms-tourism.com`)
2. Set up SendGrid Domain Authentication
3. Add DNS records (CNAME) to domain provider
4. Update `.env` with verified domain email

### Option 2: Paid SendGrid Plan

- Dedicated IP address
- Better email deliverability
- Higher reputation score

### Option 3: Alternative Email Service

- AWS SES (Simple Email Service)
- Mailgun
- Postmark

## Demo Instructions (For Instructors)

When demonstrating this feature:

1. **Start the API**: `.\mvnw.cmd spring-boot:run`
2. **Send reset request**: Use Postman or frontend
3. **Show console logs**:
   ```
   ✅ Successfully sent password reset email
   📧 Email sent! May arrive in spam/junk folder
   💡 TIP: Check spam folder and mark as 'Not Spam'
   ```
4. **Open email client**: Check spam folder
5. **Show email**: Professional HTML template with reset button
6. **Complete reset**: Click button → Enter new password
7. **Show confirmation**: Second email confirming password change

## Technical Notes

### Why Plain Text + HTML?

- Email providers prefer multipart emails (text + HTML)
- Improves deliverability and spam score
- Accessible for text-only email clients

### Why [TMS Tourism] Prefix?

- Easy to identify in spam folder
- Professional appearance
- Follows email best practices

### Security Features

- ✅ Tokens expire after 15 minutes
- ✅ One-time use tokens
- ✅ Secure token generation (UUID)
- ✅ Email validation before sending
- ✅ Account locked/deleted check

## Troubleshooting

### Email Not Received (Even in Spam)?

1. Check SendGrid dashboard for delivery status
2. Verify API key is correct
3. Check sender email is verified in SendGrid
4. Review server logs for errors

### SendGrid 403 Error?

```
The from address does not match a verified Sender Identity
```

**Solution**: Verify sender email in SendGrid settings

### Token Expired?

- Tokens are valid for 15 minutes only
- Request a new password reset

## Support

For issues related to email delivery:

- **SendGrid Dashboard**: https://app.sendgrid.com/
- **SendGrid Docs**: https://docs.sendgrid.com/
- **Project Issues**: Contact development team

---

**Note**: This is a student project for educational purposes at University of Information Technology (UIT).
