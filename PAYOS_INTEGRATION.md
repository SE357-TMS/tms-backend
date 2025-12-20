# PayOS Payment Integration

This document describes the PayOS payment integration for the TMS application.

## Overview

The PayOS integration allows customers to pay for tour bookings using bank transfers or e-wallets through QR code scanning or direct payment links.

## Features

- **QR Code Payment**: Generate QR codes for bank transfer payments
- **Payment Link**: Direct payment URLs for e-wallet transactions
- **Payment Status Tracking**: Real-time payment verification
- **Automatic Booking Updates**: Booking status automatically updates upon successful payment
- **Persistent Payment Sessions**: Payment URLs are stored in browser localStorage
- **Logout Cleanup**: Pending payments are automatically cancelled on logout

## Configuration

### Backend Configuration

Add the following environment variables to your `.env` file:

```env
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
PAYOS_RETURN_URL=http://localhost:5173/payment
PAYOS_CANCEL_URL=http://localhost:5173/cart
```

### Demo Mode

For testing purposes, the payment amount is divided by 1000 to allow testing with smaller amounts:

```javascript
const amount = Math.floor((orderInfo.totalPrice || 0) / 1000);
```

## Frontend Implementation

### Payment Flow

1. Customer selects "Bank Transfer" or "E-Wallet" payment method
2. Clicks "Generate Payment" button
3. PayOS payment link is created via API
4. Modal displays QR code and payment URL
5. Customer completes payment using banking app or e-wallet
6. Customer clicks "I Have Paid" to verify payment
7. System verifies payment status with PayOS
8. Booking status updates to "PAID" if successful

### Payment Modal

The payment modal displays:

- Payment amount and order code
- QR code for scanning
- Link to open payment page in new tab
- Bank account details (if available)
- Options to cancel payment or verify completion

### LocalStorage Management

```javascript
// Store payment URL
paymentService.storePaymentUrl(bookingId, paymentData);

// Retrieve stored payment
const stored = paymentService.getStoredPaymentUrl(bookingId);

// Clear payment URL
paymentService.clearStoredPaymentUrl(bookingId);

// Clear all on logout
paymentService.clearAllStoredPayments();
```

## Backend Implementation

### API Endpoints

#### 1. Create Payment Link

```
POST /api/v1/payment/create-payment
```

Request body:

```json
{
  "bookingId": "uuid",
  "amount": 50000,
  "description": "Tour booking",
  "buyerName": "John Doe",
  "buyerEmail": "john@example.com",
  "buyerPhone": "0901234567"
}
```

Response:

```json
{
  "code": "00",
  "message": "Payment link created successfully",
  "data": {
    "orderCode": 1234567890,
    "amount": 50000,
    "checkoutUrl": "https://pay.payos.vn/...",
    "qrCode": "https://api.payos.vn/qr/...",
    "status": "PENDING"
  }
}
```

#### 2. Get Payment Info

```
GET /api/v1/payment/payment-requests/{orderCode}
```

#### 3. Cancel Payment Link

```
POST /api/v1/payment/payment-requests/{orderCode}/cancel
```

Request body:

```json
{
  "cancellationReason": "User cancelled"
}
```

#### 4. Verify and Update Payment

```
POST /api/v1/payment/verify-payment
```

Request body:

```json
{
  "bookingId": "uuid",
  "orderCode": 1234567890
}
```

#### 5. Webhook (for PayOS callbacks)

```
POST /api/v1/payment/webhook
```

## Services

### Frontend: `paymentService.js`

Methods:

- `createPaymentLink(data)` - Create new payment link
- `getPaymentInfo(orderCode)` - Get payment status
- `cancelPaymentLink(orderCode, reason)` - Cancel payment
- `verifyAndUpdatePayment(bookingId, orderCode)` - Verify and update booking
- `storePaymentUrl(bookingId, data)` - Store in localStorage
- `getStoredPaymentUrl(bookingId)` - Retrieve from localStorage
- `clearStoredPaymentUrl(bookingId)` - Clear specific payment
- `clearAllStoredPayments()` - Clear all stored payments

### Backend: `PayOSServiceImpl.java`

Methods:

- `createPaymentLink(request)` - Create PayOS payment link
- `getPaymentInfo(orderCode)` - Fetch payment details from PayOS
- `cancelPaymentLink(orderCode, reason)` - Cancel payment on PayOS
- `verifyAndUpdatePayment(bookingId, orderCode)` - Verify payment and update booking
- `handleWebhook(webhookData)` - Process PayOS webhook callbacks

## Payment Status Flow

```
PENDING → PAID → Booking Status: CONFIRMED
         ↓
      CANCELLED
```

## Security Considerations

1. **Signature Verification**: All PayOS webhooks are verified using HMAC SHA256
2. **Authentication**: Payment endpoints require user authentication
3. **Authorization**: Users can only create payments for their own bookings
4. **HTTPS Only**: Production must use HTTPS for payment URLs

## Testing

### Test Payment Flow

1. Create a booking
2. Navigate to payment page
3. Select "Bank Transfer" or "E-Wallet"
4. Click "Generate Payment"
5. View QR code modal
6. Use PayOS sandbox credentials to test payment
7. Click "I Have Paid" to verify
8. Booking status should update to PAID

### Webhook Testing

Use ngrok or similar tool to expose your local backend:

```bash
ngrok http 8081
```

Configure webhook URL in PayOS dashboard:

```
https://your-ngrok-url.ngrok.io/api/v1/payment/webhook
```

## Troubleshooting

### Payment Link Not Generating

- Check PayOS credentials in `.env`
- Verify booking exists and amount is valid
- Check backend logs for API errors

### QR Code Not Displaying

- Ensure PayOS returns `qrCode` field
- Check network tab for API response
- Verify image URL is accessible

### Payment Not Updating Booking

- Check webhook is configured correctly
- Verify payment status with PayOS dashboard
- Check backend logs for verification errors

### localStorage Issues

- Clear browser cache and localStorage
- Check for storage quota limits
- Verify bookingId is correct

## Dependencies

### Frontend

- Axios (for API calls)
- SweetAlert2 (for notifications)

### Backend

- PayOS Java SDK v1.0.8
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA

## Support

For PayOS-specific issues, refer to:

- PayOS Documentation: https://payos.vn/docs
- PayOS Support: support@payos.vn
