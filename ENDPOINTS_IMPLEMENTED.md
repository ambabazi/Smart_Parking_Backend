# Smart Parking - Newly Implemented Endpoints

This document lists all the endpoints added to complete the MVP. These are available in Swagger UI and the OpenAPI spec at `/api-docs`.

## Authentication Endpoints

### Get Current User Profile
- **Endpoint:** `GET /api/auth/me`
- **Auth Required:** YES (Bearer token)
- **Allowed Roles:** Any authenticated user
- **Response:** User profile with ID, name, email, phone, role, and creation date
- **Use Case:** Frontend needs to display logged-in user info

---

## Reservation Endpoints

### Get My Reservations (Driver)
- **Endpoint:** `GET /reservations/my`
- **Auth Required:** YES
- **Allowed Roles:** DRIVER
- **Response:** List of all reservations made by the logged-in driver
- **Use Case:** Driver sees their booking history

### Get Specific Reservation
- **Endpoint:** `GET /reservations/{id}`
- **Auth Required:** YES
- **Allowed Roles:** Any authenticated user (can view if it's theirs)
- **Path Parameter:** `id` = reservation ID
- **Response:** Full reservation details
- **Use Case:** View booking details

### Create Reservation (Existing)
- **Endpoint:** `POST /reservations`
- **Auth Required:** YES
- **Allowed Roles:** DRIVER
- **Body:**
  ```json
  {
    "parkingSpaceId": 1,
    "slotCount": 1,
    "startTime": "2026-05-13T13:00:00",
    "endTime": "2026-05-13T15:00:00"
  }
  ```
- **Response:** Created reservation with ID
- **Use Case:** Driver books a parking space

### Cancel Reservation
- **Endpoint:** `PATCH /reservations/{id}/cancel`
- **Auth Required:** YES
- **Allowed Roles:** DRIVER (only their own)
- **Path Parameter:** `id` = reservation ID
- **Response:** Cancelled reservation details
- **Use Case:** Driver cancels their booking (slots returned to parking space)

### Get Active Reservations (Host)
- **Endpoint:** `GET /reservations/active`
- **Auth Required:** YES
- **Allowed Roles:** HOST or ADMIN
- **Response:** List of all currently active reservations at spaces owned by the logged-in host
- **Use Case:** Host sees who is parked at their locations right now

---

## Parking Space Management Endpoints (For HOST Users)

### Register a Parking Space
- **Endpoint:** `POST /api/parking`
- **Auth Required:** YES
- **Allowed Roles:** HOST
- **Body:**
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
- **Response:** Created parking space details with owner info
- **Use Case:** HOST registers their parking location

### Get My Parking Spaces
- **Endpoint:** `GET /api/parking/mine`
- **Auth Required:** YES
- **Allowed Roles:** HOST
- **Response:** List of all parking spaces owned by the logged-in host
- **Use Case:** HOST views all their registered parking locations

### Get Specific Parking Space Details
- **Endpoint:** `GET /api/parking/{id}`
- **Auth Required:** NO (public)
- **Path Parameter:** `id` = parking space ID
- **Response:** Parking space details including owner, pricing, available slots
- **Use Case:** Anyone view parking space info when searching

### Update Parking Space
- **Endpoint:** `PUT /api/parking/{id}`
- **Auth Required:** YES
- **Allowed Roles:** HOST (only their own)
- **Path Parameter:** `id` = parking space ID
- **Body:** Same as registration (can update all fields)
- **Response:** Updated parking space details
- **Use Case:** HOST modifies pricing, name, or details of their space

### Delete Parking Space
- **Endpoint:** `DELETE /api/parking/{id}`
- **Auth Required:** YES
- **Allowed Roles:** HOST (only their own)
- **Path Parameter:** `id` = parking space ID
- **Response:** Success message
- **Use Case:** HOST removes a parking space listing

---

## Admin Dashboard Endpoint

### Get Dashboard Statistics
- **Endpoint:** `GET /api/admin/dashboard`
- **Auth Required:** YES
- **Allowed Roles:** ADMIN only
- **Response:**
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
- **Use Case:** Admin sees system-wide metrics and KPIs

---

## Notification Endpoints

### Send SMS (Admin/Internal Use)
- **Endpoint:** `POST /api/ussd/sms`
- **Auth Required:** YES
- **Allowed Roles:** ADMIN
- **Body:**
  ```json
  {
    "phoneNumber": "+250712345678",
    "message": "Your parking reservation at X is expiring soon"
  }
  ```
- **Response:** Confirmation that SMS was sent
- **Use Case:** Admin/system triggers custom SMS notifications

### USSD Menu (Public - No Auth)
- **Endpoint:** `POST /api/ussd`
- **Auth Required:** NO
- **Body:** Form data from Africa's Talking callback
    - `sessionId`
    - `phoneNumber`
    - `serviceCode`
    - `text`
- **Response:** Menu text response (USSD protocol)
- **Use Case:** Basic phone users dial *384# to book parking via USSD

---

## Key Database Changes

An owner-parking relationship was added:
- **Migration V4:** Added `owner_id` column to `parking_spaces` table
- **Effect:** Each parking space now belongs to one HOST/owner
- **Automatic:** Applied on app startup via Flyway

---

## Summary of What Was Added

| Category | Endpoints Added |
|----------|-----------------|
| **Auth** | 1 (GET /api/auth/me) |
| **Reservations** | 4 (my, get detail, cancel, active) |
| **Parking** | 5 (register, my list, detail, update, delete) |
| **Admin** | 1 (dashboard) |
| **Notifications** | 1 (SMS trigger) |
| **USSD** | Already existed, SMS endpoint added |
| **Total** | 13 new endpoints |

---

## Testing in Swagger

1. **Register:** `POST /api/auth/register` with fullName, email, phone, password, role
2. **Login/Get Token:** `POST /api/auth/login` with email and password
3. **Click Authorize:** Use the Bearer token in Swagger
4. **Test endpoints:** All authenticated endpoints become available

For HOST testing:
- Register with `"role": "HOST"`
- Register a parking space with `POST /api/parking`
- View your spaces with `GET /api/parking/mine`

For DRIVER testing:
- Register with `"role": "DRIVER"` (default)
- Create a reservation with `POST /reservations`
- View your reservations with `GET /reservations/my`
- Cancel with `PATCH /reservations/{id}/cancel`

For HOST to see active:
- Use the HOST account token
- Call `GET /reservations/active` to see who's parked now

For ADMIN:
- Register with `"role": "ADMIN"`
- View dashboard with `GET /api/admin/dashboard`
- Send SMS with `POST /api/ussd/sms`
