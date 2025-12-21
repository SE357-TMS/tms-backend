# Booking Management API Documentation

## Overview

This document describes the API endpoints for managing tour bookings in the TMS (Tour Management System).

## Base URL

```
/api/v1/tour-bookings
```

## Authentication

All endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. Create Booking

Create a new tour booking.

**URL:** `POST /api/v1/tour-bookings`

**Authorization:** Authenticated users

**Request Body:**

```json
{
	"tripId": "uuid",
	"userId": "uuid",
	"noAdults": 2,
	"noChildren": 1,
	"travelers": [
		{
			"fullName": "John Doe",
			"gender": "M",
			"dateOfBirth": "1990-01-15",
			"identityDoc": "123456789"
		},
		{
			"fullName": "Jane Doe",
			"gender": "F",
			"dateOfBirth": "1992-05-20",
			"identityDoc": "987654321"
		},
		{
			"fullName": "Junior Doe",
			"gender": "M",
			"dateOfBirth": "2015-08-10",
			"identityDoc": "111222333"
		}
	]
}
```

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": "uuid",
    "tripId": "uuid",
    "routeName": "Hanoi - Da Nang",
    "departureDate": "2024-02-15",
    "returnDate": "2024-02-20",
    "userId": "uuid",
    "userName": "Customer Name",
    "userEmail": "customer@example.com",
    "seatsBooked": 3,
    "totalPrice": 15000000,
    "status": "PENDING",
    "noAdults": 2,
    "noChildren": 1,
    "travelers": [...],
    "invoice": {
      "id": "uuid",
      "totalAmount": 15000000,
      "paymentStatus": "UNPAID",
      "paymentMethod": null
    },
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": "2024-01-20T10:30:00"
  }
}
```

**Validation Rules:**

- Trip must exist and be in SCHEDULED status
- Trip departure date must be at least 2 days from now
- User must exist
- Number of travelers must match `noAdults + noChildren`
- Available seats must be sufficient

---

### 2. Get Booking by ID

Retrieve detailed information about a specific booking.

**URL:** `GET /api/v1/tour-bookings/{id}`

**Authorization:** Authenticated users

**Response:** `200 OK`

```json
{
	"success": true,
	"message": "Booking retrieved successfully",
	"data": {
		"id": "uuid",
		"tripId": "uuid",
		"routeName": "Hanoi - Da Nang",
		"departureDate": "2024-02-15",
		"returnDate": "2024-02-20",
		"userId": "uuid",
		"userName": "Customer Name",
		"userEmail": "customer@example.com",
		"seatsBooked": 3,
		"totalPrice": 15000000,
		"status": "PENDING",
		"noAdults": 2,
		"noChildren": 1,
		"travelers": [
			{
				"id": "uuid",
				"fullName": "John Doe",
				"gender": "M",
				"dateOfBirth": "1990-01-15",
				"identityDoc": "123456789"
			}
		],
		"invoice": {
			"id": "uuid",
			"totalAmount": 15000000,
			"paymentStatus": "UNPAID",
			"paymentMethod": null
		},
		"createdAt": "2024-01-20T10:30:00",
		"updatedAt": "2024-01-20T10:30:00"
	}
}
```

---

### 3. Get All Bookings (Paginated)

Retrieve a paginated list of bookings with optional filters.

**URL:** `GET /api/v1/tour-bookings`

**Authorization:** ADMIN, STAFF only

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 1 | Page number (1-based) |
| `pageSize` | Integer | 10 | Number of items per page |
| `sortBy` | String | "createdAt" | Field to sort by |
| `sortDirection` | String | "desc" | Sort direction: "asc" or "desc" |
| `keyword` | String | null | Search by customer name, email, or route name |
| `status` | String | null | Filter by status: PENDING, CONFIRMED, CANCELED, COMPLETED |
| `userId` | UUID | null | Filter by user ID |
| `tripId` | UUID | null | Filter by trip ID |
| `fromDate` | Date | null | Filter bookings created from this date (YYYY-MM-DD) |
| `toDate` | Date | null | Filter bookings created until this date (YYYY-MM-DD) |
| `departureFrom` | Date | null | Filter by trip departure date from (YYYY-MM-DD) |
| `departureTo` | Date | null | Filter by trip departure date until (YYYY-MM-DD) |

**Example Request:**

```
GET /api/v1/tour-bookings?page=1&pageSize=10&keyword=john&status=PENDING&sortBy=createdAt&sortDirection=desc
```

**Response:** `200 OK`

```json
{
	"success": true,
	"message": "Bookings retrieved successfully",
	"data": {
		"totalElements": 50,
		"totalPages": 5,
		"currentPage": 1,
		"pageSize": 10,
		"items": [
			{
				"id": "uuid",
				"routeName": "Hanoi - Da Nang",
				"userName": "John Doe",
				"seatsBooked": 3,
				"totalPrice": 15000000,
				"status": "PENDING",
				"createdAt": "2024-01-20T10:30:00"
			}
		]
	}
}
```

---

### 4. Get Bookings by User ID

Retrieve all bookings for a specific user.

**URL:** `GET /api/v1/tour-bookings/user/{userId}`

**Authorization:** Authenticated users

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "routeName": "Hanoi - Da Nang",
      "status": "PENDING",
      ...
    }
  ]
}
```

---

### 5. Update Booking

Update booking status and/or travelers.

**URL:** `PUT /api/v1/tour-bookings/{id}`

**Authorization:** ADMIN, STAFF only

**Request Body:**

```json
{
	"status": "CONFIRMED",
	"travelers": [
		{
			"fullName": "John Doe Updated",
			"gender": "M",
			"dateOfBirth": "1990-01-15",
			"identityDoc": "123456789"
		}
	],
	"noAdults": 1,
	"noChildren": 0,
	"notes": "VIP customer"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Booking updated successfully",
  "data": { ... }
}
```

**Business Rules for Editing Travelers:**

- Cannot edit travelers after trip departure date
- Cannot edit canceled or completed bookings
- If number of travelers changes, seat availability is rechecked
- Invoice amount is automatically recalculated

---

### 6. Cancel Booking

Cancel a booking and return seats to the trip.

**URL:** `POST /api/v1/tour-bookings/{id}/cancel`

**Authorization:** Authenticated users

**Response:** `200 OK`

```json
{
	"success": true,
	"message": "Booking canceled successfully"
}
```

**Business Rules:**

- Cannot cancel already canceled bookings
- Cannot cancel completed bookings
- Seats are returned to the trip's available pool
- If invoice was PAID, it is marked as REFUNDED

---

### 7. Delete Booking

Soft delete a booking.

**URL:** `DELETE /api/v1/tour-bookings/{id}`

**Authorization:** ADMIN only

**Response:** `200 OK`

```json
{
	"success": true,
	"message": "Booking deleted successfully"
}
```

---

## Booking Status Values

| Status      | Description                            |
| ----------- | -------------------------------------- |
| `PENDING`   | Booking created, awaiting confirmation |
| `CONFIRMED` | Booking confirmed by staff             |
| `CANCELED`  | Booking has been canceled              |
| `COMPLETED` | Trip has been completed                |

---

## Invoice Payment Status Values

| Status     | Description               |
| ---------- | ------------------------- |
| `UNPAID`   | Payment not yet received  |
| `PAID`     | Payment received          |
| `REFUNDED` | Payment has been refunded |

---

## Error Responses

### 400 Bad Request

```json
{
	"success": false,
	"message": "Not enough available seats. Available: 5"
}
```

### 404 Not Found

```json
{
	"success": false,
	"message": "Booking not found"
}
```

### 403 Forbidden

```json
{
	"success": false,
	"message": "Access denied"
}
```

---

## Use Cases Mapping

| Use Case                          | Endpoint                       | Description                |
| --------------------------------- | ------------------------------ | -------------------------- |
| UC39 - Add Booking                | `POST /`                       | Staff creates new booking  |
| UC40 - View Booking Detail        | `GET /{id}`                    | View complete booking info |
| UC41 - Edit Pre-departure Booking | `PUT /{id}` with travelers     | Edit travelers before trip |
| UC42 - Cancel Booking             | `POST /{id}/cancel`            | Cancel and refund booking  |
| UC43 - Search/Filter Bookings     | `GET /` with keyword/filters   | Search and filter list     |
| UC44 - View Invoice               | `GET /{id}` (includes invoice) | Invoice info in response   |
