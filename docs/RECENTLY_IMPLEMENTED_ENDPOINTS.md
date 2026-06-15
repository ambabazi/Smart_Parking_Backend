# Smart Parking - Newly Implemented Endpoints

This document lists the API routes currently implemented in the backend. These are available in Swagger UI and the OpenAPI spec at `/api-docs`.

It includes the Redis-backed session refresh flow, paginated list endpoints, public parking routes under `/parking-spaces`, reservation flows, payments, QR code utilities, event management, admin stats, and notification hooks (USSD/SMS).

---

## Authentication Endpoints

### Get Current User Profile
- Endpoint: `GET /api/auth/me`
- Auth Required: YES (Bearer token)
- Allowed Roles: Any authenticated user
- Response: User profile with ID, name, email, phone, role, and creation date
- Use Case: Frontend needs to display logged-in user info

### Refresh Session
- Endpoint: `POST /api/auth/refresh`
- Auth Required: NO, but requires a valid refresh token in the body
- Body:
```json
{
  "refreshToken": "<refresh-token>"
}
```
- Response: New access token plus a rotated refresh token
- Use Case: Keep users signed in without forcing a full login

### Logout
- Endpoint: `POST /api/auth/logout`
- Auth Required: NO, but requires a valid refresh token in the body
- Body:
```json
{
  "refreshToken": "<refresh-token>"
}
```
- Response: Logged-out confirmation
- Use Case: Revoke the stored refresh token from Redis

---

## Reservation Endpoints

### Get My Reservations (Driver)
- Endpoint: `GET /reservations/my?page=0&size=10`
- Auth Required: YES
- Allowed Roles: DRIVER
- Response: Paginated list of reservations made by the logged-in driver
- Use Case: Driver sees their booking history without loading everything at once

### Get Specific Reservation
- Endpoint: `GET /reservations/{id}`
- Auth Required: YES
- Allowed Roles: Any authenticated user (can view if it's theirs)
- Path Parameter: `id` = reservation ID
- Response: Full reservation details
- Use Case: View booking details

### Create Reservation (Existing)
- Endpoint: `POST /reservations`
- Auth Required: YES
- Allowed Roles: DRIVER
- Body:
```json
{
  "parkingSpaceId": 1,
  "slotCount": 1,
  "startTime": "2026-05-13T13:00:00",
  "endTime": "2026-05-13T15:00:00"
}
```
- Response: Created reservation with ID
- Use Case: Driver books a parking space

### Cancel Reservation
- Endpoint: `PATCH /reservations/{id}/cancel`
- Auth Required: YES
- Allowed Roles: DRIVER (only their own)
- Path Parameter: `id` = reservation ID
- Response: Cancelled reservation details
- Use Case: Driver cancels their booking (slots returned to parking space)

### Check In
- Endpoint: `POST /reservations/{id}/check-in`
- Auth Required: YES
- Allowed Roles: DRIVER
- Use Case: Mark the start of the stay once payment is complete

### Check Out
- Endpoint: `POST /reservations/{id}/checkout`
- Auth Required: YES
- Allowed Roles: DRIVER
- Use Case: End the stay and calculate overtime if needed

### Pay Overtime
- Endpoint: `POST /reservations/{id}/pay-overtime?amount=...`
- Auth Required: YES
- Allowed Roles: DRIVER
- Use Case: Clear an overtime balance before exit

### Get Active Reservations (Host)
- Endpoint: `GET /reservations/active?page=0&size=10`
- Auth Required: YES
- Allowed Roles: HOST or ADMIN
- Response: Paginated list of currently active reservations at spaces owned by the logged-in host
- Use Case: Host sees who is parked at their locations right now

---

## Public Parking Search Endpoints

### Get All Parking Spaces
- Endpoint: `GET /parking-spaces`
- Auth Required: NO
- Response: List of all parking spaces in the system
- Use Case: Display all available parking spaces to users

### Get Specific Parking Space Details
- Endpoint: `GET /parking-spaces/{id}`
- Auth Required: NO (public)
- Path Parameter: `id` = parking space ID
- Response: Parking space details including owner, pricing, available slots, event status
- Use Case: View detailed info for a single parking space

### Search Nearby Parking Spaces
- Endpoint: `GET /parking-spaces/nearby?lat=...&lng=...&radius=...`
- Auth Required: NO
- Query Parameters:
  - `lat` (Double) — latitude
  - `lng` (Double) — longitude
  - `radius` (Double, default 2000) — search radius in metres
- Response: List of parking spaces within radius, sorted by distance
- Use Case: Find available parking near a location

### Get Spaces Linked to an Event
- Endpoint: `GET /parking-spaces/event/{eventId}`
- Auth Required: NO
- Path Parameter: `eventId` = event ID
- Response: List of parking spaces activated for the event
- Use Case: Show which parking spaces are event-enabled

---

## Parking Space Management Endpoints (For HOST Users)

### Register a Parking Space
- Endpoint: `POST /parking-spaces`
- Auth Required: YES
- Allowed Roles: HOST
- Body:
```json
{
  "name": "Kacyiru Parking",
  "address": "Kacyiru, Kigali",
  "latitude": -1.9536,
  "longitude": 30.0605,
  "totalSlots": 50,
  "pricePerSlot": 500.0
}
```
- Response: Created parking space details with owner info
- Use Case: HOST registers their parking location

### Get My Parking Spaces
- Endpoint: `GET /parking-spaces/mine`
- Auth Required: YES
- Allowed Roles: HOST
- Response: List of all parking spaces owned by the logged-in host
- Use Case: HOST views all their registered parking locations

### Update Parking Space
- Endpoint: `PUT /parking-spaces/{id}`
- Auth Required: YES
- Allowed Roles: HOST (only their own)
- Path Parameter: `id` = parking space ID
- Body: Same as registration (can update all fields)
- Response: Updated parking space details
- Use Case: HOST modifies pricing, name, or details of their space

### Delete Parking Space
- Endpoint: `DELETE /parking-spaces/{id}`
- Auth Required: YES
- Allowed Roles: HOST (only their own)
- Path Parameter: `id` = parking space ID
- Response: Success message
- Use Case: HOST removes a parking space listing

---

## Event Management Endpoints

### Create Event
- Endpoint: `POST /events`
- Auth Required: YES
- Allowed Roles: ADMIN
- Body:
```json
{
  "name": "Kigali Arena Concert",
  "latitude": -1.9500,
  "longitude": 30.1000,
  "radiusMetres": 5000,
  "startTime": "2026-05-15T18:00:00",
  "endTime": "2026-05-15T23:00:00"
}
```
- Response: Created event with ID and number of activated spaces
- Use Case: Admin creates a special event (concert, festival) and auto-activates all nearby parking spaces

### Get Active Events
- Endpoint: `GET /events/active`
- Auth Required: NO
- Response: List of events currently in progress (startTime ≤ now ≤ endTime)
- Use Case: Show active events on the map to users

### Deactivate Event
- Endpoint: `DELETE /events/{id}/deactivate`
- Auth Required: YES
- Allowed Roles: ADMIN
- Path Parameter: `id` = event ID
- Response: 204 No Content (empty response)
- Use Case: End an event and reset all linked parking spaces back to normal

---

## QR Code Endpoints

### Generate QR Code
- Endpoint: `GET /api/qr/generate?reservationId=...&userId=...`
- Auth Required: YES
- Allowed Roles: Any authenticated user
- Query Parameters:
  - `reservationId` (Long) — reservation ID
  - `userId` (Long) — user ID
- Response: PNG image (binary)
- Use Case: Generate QR code for reservation verification at entry

### Verify QR Code
- Endpoint: `POST /api/qr/verify`
- Auth Required: YES
- Allowed Roles: HOST or ADMIN
- Body:
```json
{
  "qrContent": "<scanned-qr-string>",
  "reservationId": 1
}
```
- Response: Boolean true/false indicating if QR is valid
- Use Case: HOST scans driver's QR code at parking entry to verify reservation

---

## Payment Endpoints

### Initiate Payment
- Endpoint: `POST /payments/initiate/{reservationId}`
- Auth Required: YES
- Allowed Roles: DRIVER
- Path Parameter: `reservationId` = reservation ID
- Response: Payment URL (Flutterwave checkout link)
- Use Case: DRIVER initiates payment for their reservation

### Payment Webhook (Flutterwave)
- Endpoint: `POST /payments/webhook`
- Auth Required: NO (secured via signature verification)
- Body: Flutterwave webhook event
- Response: 200 OK
- Use Case: Flutterwave calls this endpoint to confirm payment status

---

## Admin Dashboard Endpoint

### Get Dashboard Statistics
- Endpoint: `GET /api/admin/dashboard`
- Auth Required: YES
- Allowed Roles: ADMIN only
- Response:
```json
{
  "totalParkingSpaces": 15,
  "totalReservationSlots": 500,
  "activeReservations": 42,
  "bookingsToday": 28,
  "revenueToday": 45000.00,
  "occupancyPercentage": 84.5
}
```
- Use Case: Admin sees system-wide metrics and KPIs

---

## Notification Endpoints

### Send SMS (Admin/Internal Use)
- Endpoint: `POST /api/ussd/sms`
- Auth Required: YES
- Allowed Roles: ADMIN
- Body:
```json
{
  "phoneNumber": "+250712345678",
  "message": "Your parking reservation at X is expiring soon"
}
```
- Response: Confirmation that SMS was sent
- Use Case: Admin/system triggers custom SMS notifications

### USSD Menu (Public - No Auth)
- Endpoint: `POST /api/ussd`
- Auth Required: NO
- Body: Form data from Africa's Talking callback
  - `sessionId`
  - `phoneNumber`
  - `serviceCode`
  - `text`
- Response: Menu text response (USSD protocol)
- Use Case: Basic phone users dial *384# to book parking via USSD

---

## Key Database Changes

An owner-parking relationship was added:
- Migration V4: Added `owner_id` column to `parking_spaces` table
- Effect: Each parking space now belongs to one HOST/owner
- Automatic: Applied on app startup via Flyway

---

## Summary of What Was Added

| Category | Endpoints Added |
|----------|-----------------|
| **Auth** | 3 (me, refresh, logout) |
| **Reservations** | 8 (create, my, detail, cancel, check-in, checkout, overtime, active) |
| **Parking (Public)** | 4 (list all, detail, nearby, by event) |
| **Parking (HOST)** | 4 (register, my list, update, delete) |
| **Events** | 3 (create, get active, deactivate) |
| **QR Code** | 2 (generate, verify) |
| **Payment** | 2 (initiate, webhook) |
| **Admin** | 1 (dashboard) |
| **Notifications** | 2 (SMS trigger, USSD) |
| **Total** | 29 endpoints |

---

## Testing in Swagger

1. Register: `POST /api/auth/register` with `fullName`, `email`, `phone`, `password`, `role`
2. Login/Get Token: `POST /api/auth/login` with `email` and `password`
3. Click Authorize: Use the Bearer token in Swagger
4. Refresh if needed: `POST /api/auth/refresh` with the refresh token from login
5. Logout when done: `POST /api/auth/logout` to revoke the refresh token
6. Test endpoints: All authenticated endpoints become available

### Quick role-based testing notes
- For HOST testing: register with `"role": "HOST"` and use `POST /parking-spaces`, `GET /parking-spaces/mine`, `PUT /parking-spaces/{id}`
- For DRIVER testing: register with `"role": "DRIVER"` (default) and use `POST /reservations`, `GET /reservations/my`, `PATCH /reservations/{id}/cancel`, `POST /reservations/{id}/check-in`, `POST /reservations/{id}/checkout`
- For ADMIN: register with `"role": "ADMIN"` and use `POST /events`, `DELETE /events/{id}/deactivate`, `GET /api/admin/dashboard`, `POST /api/ussd/sms`
