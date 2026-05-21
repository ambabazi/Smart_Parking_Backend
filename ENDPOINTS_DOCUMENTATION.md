# Smart Parking Backend - Complete API Endpoint Documentation

**Base URL**: `http://localhost:8080` (local) or `https://smart-parking-backend-2.onrender.com` (production)

---

## Table of Contents
1. [Authentication](#authentication)
2. [Parking Spaces](#parking-spaces)
3. [Reservations](#reservations)
4. [QR Code Management](#qr-code-management)
5. [Payments](#payments)
6. [Events](#events)
7. [Dashboards](#dashboards)
8. [Admin](#admin)
9. [Notifications](#notifications)
10. [Health & Monitoring](#health--monitoring)

---

## Authentication

All endpoints (except public ones) require `Authorization: Bearer <JWT_TOKEN>` header.

### `POST /api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "John Doe",
  "phone": "+250788000000"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGc...",
    "refreshToken": "uuid-string",
    "type": "Bearer",
    "userId": 1,
    "fullName": "John Doe",
    "email": "user@example.com",
    "role": "DRIVER",
    "accessTokenExpiresInMs": 900000,
    "refreshTokenExpiresInMs": 604800000
  }
}
```

---

### `POST /api/auth/login`
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGc...",
    "refreshToken": "uuid-string",
    "type": "Bearer",
    "userId": 1,
    "fullName": "John Doe",
    "email": "user@example.com",
    "role": "DRIVER"
  }
}
```

---

### `POST /api/auth/refresh`
Refresh expired JWT token.

**Request Body:**
```json
{
  "refreshToken": "uuid-string"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "token": "new-jwt-token",
    "refreshToken": "new-uuid-string",
    "type": "Bearer"
  }
}
```

---

### `POST /api/auth/logout`
Invalidate refresh token and logout.

**Request Body:**
```json
{
  "refreshToken": "uuid-string"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

### `GET /api/auth/me`
Get current authenticated user profile.

**Headers:** `Authorization: Bearer <JWT_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "phone": "+250788000000",
    "role": "DRIVER",
    "createdAt": "2026-05-21T20:30:00Z"
  }
}
```

---

## Parking Spaces

### `GET /parking-spaces` (Public)
List all available parking spaces with pagination.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Items per page (default: 10)
- `sort` (optional): Sort field (default: id)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Downtown Lot A",
      "address": "123 Main Street, Kigali",
      "latitude": -1.9473,
      "longitude": 30.0583,
      "totalSlots": 50,
      "availableSlots": 32,
      "pricePerSlot": 500,
      "ownerId": 5,
      "ownerName": "Parking Co.",
      "description": "Secure parking facility"
    }
  ],
  "pageable": {...},
  "totalElements": 45,
  "totalPages": 5
}
```

---

### `GET /parking-spaces/{id}` (Public)
Get specific parking space details.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Downtown Lot A",
  "address": "123 Main Street, Kigali",
  "latitude": -1.9473,
  "longitude": 30.0583,
  "totalSlots": 50,
  "availableSlots": 32,
  "pricePerSlot": 500,
  "ownerId": 5,
  "ownerName": "Parking Co.",
  "description": "Secure parking facility",
  "createdAt": "2026-05-15T10:00:00Z"
}
```

---

### `GET /parking-spaces/nearby` (Public)
Find parking spaces within a radius.

**Query Parameters:**
- `lat` (required): Latitude
- `lng` (required): Longitude
- `radius` (optional): Radius in meters (default: 2000)
- `page` (optional): Page number
- `size` (optional): Items per page

**Example:** `GET /parking-spaces/nearby?lat=-1.9473&lng=30.0583&radius=5000`

---

### `GET /parking-spaces/event/{eventId}` (Public)
Get parking spaces for a specific event.

**Response:** Same as `/parking-spaces`

---

### `POST /parking-spaces` (HOST only)
Register a new parking space.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Request Body:**
```json
{
  "name": "Downtown Lot B",
  "address": "456 Main Street, Kigali",
  "latitude": -1.9480,
  "longitude": 30.0590,
  "totalSlots": 30,
  "pricePerSlot": 600,
  "description": "Air-conditioned parking"
}
```

**Response (201 CREATED):**
```json
{
  "success": true,
  "message": "Parking space registered",
  "data": {
    "id": 46,
    "name": "Downtown Lot B",
    "address": "456 Main Street, Kigali",
    "latitude": -1.9480,
    "longitude": 30.0590,
    "totalSlots": 30,
    "availableSlots": 30,
    "pricePerSlot": 600
  }
}
```

---

### `GET /parking-spaces/mine` (HOST only)
Get all parking spaces owned by current user.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Response:** Paginated list of owned parking spaces

---

### `PUT /parking-spaces/{id}` (HOST only)
Update parking space details.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Request Body:** Same as POST

**Response (200 OK):** Updated parking space details

---

### `DELETE /parking-spaces/{id}` (HOST only)
Delete a parking space.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Parking space deleted"
}
```

---

## Reservations

### `POST /reservations` (DRIVER only)
Create a new reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Request Body:**
```json
{
  "parkingSpaceId": 1,
  "startTime": "2026-05-25T10:00:00",
  "endTime": "2026-05-25T14:00:00",
  "slotCount": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation created",
  "data": {
    "id": 100,
    "userId": 2,
    "parkingSpaceId": 1,
    "parkingSpaceName": "Downtown Lot A",
    "slotCount": 1,
    "startTime": "2026-05-25T10:00:00",
    "endTime": "2026-05-25T14:00:00",
    "totalAmount": 500,
    "paid": false,
    "qrCode": "uuid-string",
    "status": "RESERVED"
  }
}
```

---

### `GET /reservations/my` (DRIVER only)
Get all reservations for current driver.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Query Parameters:**
- `page` (optional): Page number
- `size` (optional): Items per page

**Response:** Paginated list of driver's reservations

---

### `GET /reservations/active` (HOST/ADMIN only)
Get all active reservations for current owner's parking spaces.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Query Parameters:**
- `page` (optional): Page number
- `size` (optional): Items per page

**Response:** Paginated list of active reservations

---

### `GET /reservations/{id}` (Authenticated)
Get specific reservation details.

**Headers:** `Authorization: Bearer <TOKEN>`

**Response (200 OK):** Reservation details

---

### `POST /reservations/check-in/{id}` (DRIVER only)
Check in to a reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Checked in successfully",
  "data": {
    "id": 100,
    "status": "CHECKED_IN",
    "checkedInAt": "2026-05-25T10:05:00"
  }
}
```

---

### `POST /reservations/{id}/checkout` (DRIVER only)
Check out from a reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Checked out successfully",
  "data": {
    "reservationId": 100,
    "checkedOutAt": "2026-05-25T14:15:00",
    "hasOvertime": false
  }
}
```

---

### `PATCH /reservations/{id}/cancel` (DRIVER only)
Cancel a reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation cancelled"
}
```

---

### `GET /reservations/check-availability`
Check if parking space has available slots for given time period.

**Query Parameters:**
- `parkingSpaceId` (required): Parking space ID
- `startTime` (required): Start time (ISO 8601)
- `endTime` (required): End time (ISO 8601)
- `slotCount` (optional): Number of slots needed (default: 1)

**Example:** `GET /reservations/check-availability?parkingSpaceId=1&startTime=2026-05-25T10:00:00&endTime=2026-05-25T14:00:00&slotCount=1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Availability",
  "data": {
    "available": true,
    "availableSlots": 25,
    "estimatedPrice": 2000
  }
}
```

---

### `GET /reservations/active/current` (DRIVER only)
Get current active reservation for driver.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Current reservation",
  "data": {
    "id": 100,
    "parkingSpaceName": "Downtown Lot A",
    "address": "123 Main Street, Kigali",
    "startTime": "2026-05-25T10:00:00",
    "endTime": "2026-05-25T14:00:00",
    "slotCount": 1,
    "status": "CHECKED_IN",
    "minutesRemaining": 123
  }
}
```

---

## QR Code Management

### `GET /api/qr/generate` (DRIVER only)
Generate QR code image for a reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Query Parameters:**
- `reservationId` (required): Reservation ID

**Response (200 OK):** PNG image (binary)

**Headers:**
```
Content-Type: image/png
Content-Disposition: inline; filename=qr-100.png
```

---

### `POST /api/qr/verify` (HOST/ADMIN only)
Verify a scanned QR code.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Request Body:**
```json
{
  "qrContent": "uuid-string-or-scanned-content",
  "reservationId": 100
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "VALID",
  "data": {
    "status": "VALID",
    "valid": true,
    "userFullName": "John Doe",
    "userPhone": "+250788000000",
    "detail": "Reservation is active and ready for check-in"
  }
}
```

**Possible Status Codes:**
- `VALID`: Reservation is valid and can be checked in
- `INVALID`: QR code doesn't match any reservation
- `ALREADY_USED`: Reservation already checked in
- `EXPIRED`: Reservation end time has passed
- `NOT_STARTED`: Reservation start time hasn't arrived yet
- `FORBIDDEN`: Host doesn't own the parking space

---

## Payments

### `POST /payments/initiate/{reservationId}` (DRIVER only)
Initiate payment for a reservation via Flutterwave.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "paymentUrl": "https://checkout.flutterwave.com/v3/hosted/...",
  "txRef": "TX-1234567890"
}
```

---

### `POST /payments/webhook`
Flutterwave webhook endpoint (automatic).

**Headers:**
- `verif-hash`: Flutterwave signature for security verification

**Response (200 OK):** Webhook processed

---

### `GET /payments/status/{reservationId}` (DRIVER only)
Get payment status for a reservation.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment status",
  "data": {
    "reservationId": 100,
    "status": "PAID",
    "amount": 2000,
    "paidAt": "2026-05-25T09:45:00",
    "transactionId": "FLW123456789"
  }
}
```

---

## Events

### `GET /events/active` (Public)
Get all active events.

**Query Parameters:**
- `page` (optional): Page number
- `size` (optional): Items per page

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Tech Conference 2026",
      "description": "Annual tech conference",
      "startDate": "2026-06-01",
      "endDate": "2026-06-03",
      "location": "Kigali Convention Center",
      "active": true
    }
  ],
  "pageable": {...},
  "totalElements": 5,
  "totalPages": 1
}
```

---

### `POST /events` (ADMIN only)
Create a new event.

**Headers:** `Authorization: Bearer <ADMIN_TOKEN>`

**Request Body:**
```json
{
  "name": "Tech Conference 2026",
  "description": "Annual tech conference",
  "startDate": "2026-06-01",
  "endDate": "2026-06-03",
  "location": "Kigali Convention Center"
}
```

**Response (201 CREATED):** Event details

---

### `DELETE /events/{id}/deactivate` (ADMIN only)
Deactivate an event.

**Headers:** `Authorization: Bearer <ADMIN_TOKEN>`

**Response (204 NO CONTENT)**

---

## Dashboards

### `GET /api/driver/dashboard` (DRIVER only)
Get driver-specific dashboard statistics.

**Headers:** `Authorization: Bearer <DRIVER_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Driver dashboard statistics",
  "data": {
    "totalReservations": 25,
    "completedReservations": 20,
    "activeReservations": 1,
    "cancelledReservations": 4,
    "upcomingReservations": 3,
    "totalSpent": 50000,
    "membersince": "2026-04-01T10:30:00"
  }
}
```

---

### `GET /api/owner/dashboard` (HOST/ADMIN only)
Get owner-specific dashboard statistics.

**Headers:** `Authorization: Bearer <HOST_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Owner dashboard statistics",
  "data": {
    "totalParkingSpaces": 5,
    "totalSlots": 250,
    "availableSlots": 180,
    "occupiedSlots": 70,
    "occupancyPercentage": 28.0,
    "activeReservations": 15,
    "totalReservations": 450,
    "totalRevenue": 2500000,
    "revenueToday": 125000,
    "memberSince": "2026-01-15T14:20:00"
  }
}
```

---

## Admin

### `GET /api/admin/dashboard` (ADMIN only)
Get system-wide dashboard statistics.

**Headers:** `Authorization: Bearer <ADMIN_TOKEN>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard statistics",
  "data": {
    "totalParkingSpaces": 45,
    "totalReservationSlots": 2250,
    "activeReservations": 180,
    "bookingsToday": 85,
    "revenueToday": 650000,
    "occupancyPercentage": 42.5
  }
}
```

---

## Notifications

### `POST /api/ussd` (Internal)
USSD endpoint for SMS notifications.

**Content-Type:** `application/x-www-form-urlencoded`

**Parameters:**
- `serviceCode`: Service code
- `phoneNumber`: User's phone number
- `text`: User input text
- `sessionId`: USSD session ID

---

### `POST /api/ussd/sms` (Internal)
Direct SMS notification endpoint.

---

## Health & Monitoring

### `GET /` (Public)
Health check endpoint.

**Response (200 OK):**
```
OK
```

---

### `GET /health` (Public)
Detailed health status.

**Response (200 OK):**
```
OK - All systems operational
```

---

### `GET /ping` (Public)
Simple ping to verify backend is running.

**Response (200 OK):**
```
pong
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

**Common HTTP Status Codes:**
- `200 OK` - Request succeeded
- `201 CREATED` - Resource created successfully
- `204 NO CONTENT` - Request succeeded (no response body)
- `400 BAD REQUEST` - Invalid request parameters
- `401 UNAUTHORIZED` - Missing or invalid authentication token
- `403 FORBIDDEN` - Authenticated but not authorized for this action
- `404 NOT FOUND` - Resource not found
- `500 INTERNAL SERVER ERROR` - Server error

---

## Authentication Roles

| Role | Access |
|------|--------|
| **DRIVER** | Create/view own reservations, pay, QR generation, check-in/out, view own dashboard |
| **HOST** | Register/manage parking spaces, verify QR codes, view active reservations, view owner dashboard |
| **ADMIN** | Full system access, create events, view admin dashboard, manage users |

---

## Field Validation Rules

| Field | Rules |
|-------|-------|
| **email** | Valid email format, unique |
| **password** | Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special char |
| **phone** | Must be in format `+250XXXXXXXXX` (Rwanda) |
| **pricePerSlot** | Positive number |
| **totalSlots** | Positive integer |
| **reservationTime** | End time must be after start time |

---

## Testing the API

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'

# Get parking spaces (with token)
TOKEN="eyJhbGc..."
curl -X GET "http://localhost:8080/parking-spaces?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Create reservation
curl -X POST http://localhost:8080/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "parkingSpaceId": 1,
    "startTime": "2026-05-25T10:00:00",
    "endTime": "2026-05-25T14:00:00",
    "slotCount": 1
  }'
```

