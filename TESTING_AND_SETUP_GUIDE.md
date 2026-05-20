# Smart Parking End-to-End Testing and Setup Guide

This guide shows how to test the full backend flow in Swagger, from registering as a driver or host to payment, QR verification, cancellation, notifications, and USSD. It also lists the environment variables you need so payment and notification integrations behave correctly.

## 1. What You Can Test

The current backend supports these flows:
- register and log in users
- register parking spaces as a HOST
- create, inspect, and cancel reservations as a DRIVER
- initiate payment for a reservation
- process a Flutterwave webhook to mark a reservation paid
- generate and verify a QR code
- view host active reservations
- view admin dashboard metrics
- trigger SMS notifications
- use the public USSD callback endpoint

## 2. Before You Start

Make sure the app is running with the correct database and integration settings.

Required runtime variables from [src/main/resources/application.yaml](src/main/resources/application.yaml) and [src/main/resources/application-prod.yaml](src/main/resources/application-prod.yaml):
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `FLUTTERWAVE_PUBLIC_KEY`
- `FLUTTERWAVE_SECRET_KEY`
- `FLUTTERWAVE_SECRET_HASH`
- `FLUTTERWAVE_ENCRYPTION_KEY`
- `FRONTEND_URL` if your frontend is not `http://localhost:3000`
- `AT_USERNAME`
- `AT_API_KEY`
- `AT_SENDER_ID`
- `AT_ENV`

Important notes:
- Payment initiation uses `app.flutterwave.secret.key`.
- Webhook verification uses `app.flutterwave.secret.hash` in the `verif-hash` header.
- SMS sending is currently logged by the backend, not actually sent, unless you replace the MVP logging with a real Africa's Talking integration.
- USSD is public and expects Africa's Talking style form data.

## 3. Swagger Login Flow

1. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.
2. Register a user with `POST /api/auth/register`.
3. Log in with `POST /api/auth/login`.
4. Copy the JWT from the response.
5. Click `Authorize` in Swagger and paste the token as a Bearer token.
6. Use `GET /api/auth/me` to confirm the token works.

Example registration body:
```json
{
  "fullName": "Jane Owner",
  "email": "owner@example.com",
  "phone": "+250788123456",
  "password": "Password123!",
  "role": "HOST"
}
```

Example driver registration body:
```json
{
  "fullName": "John Driver",
  "email": "driver@example.com",
  "phone": "+250788654321",
  "password": "Password123!",
  "role": "DRIVER"
}
```

## 4. HOST Flow: Register and Manage Parking Spaces

Use a HOST account for these endpoints.

### Register a parking space
- `POST /api/parking`
- Body example:
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

### View your spaces
- `GET /api/parking/mine`

### Update a space
- `PUT /api/parking/{id}`
- Use the same body shape as registration.

### Delete a space
- `DELETE /api/parking/{id}`

### View a public space detail
- `GET /api/parking/{id}`
- No auth is required for this one.

## 5. DRIVER Flow: Search, Reserve, Pay, Cancel

Use a DRIVER account for booking endpoints.

### Create a reservation
- `POST /reservations`
- Example body:
```json
{
  "parkingSpaceId": 1,
  "slotCount": 1,
  "startTime": "2026-05-14T10:00:00",
  "endTime": "2026-05-14T12:00:00"
}
```

What should happen:
- available slots are reduced
- a reservation record is created
- a QR code token is stored on the reservation

### View your reservations
- `GET /reservations/my`

### View one reservation
- `GET /reservations/{id}`

### Cancel a reservation
- `PATCH /reservations/{id}/cancel`
- Only the owner of the reservation should call this.
- Canceling returns the reserved slots to the parking space.

### Initiate payment
- `POST /payments/initiate/{reservationId}`

What should happen:
- the backend creates a Flutterwave payment link
- the response contains `paymentUrl`
- open the link and complete payment in Flutterwave sandbox or live mode

Important:
- if the reservation is already marked paid, the endpoint returns a bad request
- if Flutterwave is not configured correctly, the payment link may fail to generate

### Process the payment webhook
- `POST /payments/webhook`

This endpoint is called by Flutterwave after payment.

Required header:
- `verif-hash: <your FLUTTERWAVE_SECRET_HASH>`

The webhook body should contain a successful payment event with a `txRef` like `KP-12`.

When the webhook is valid and successful:
- the reservation is marked as paid
- a payment record is stored

## 6. QR Flow: Generate and Verify

### Generate a QR code
- `GET /api/qr/generate?reservationId=1&userId=2`
- Auth required

This returns a PNG image of the QR code.

### Verify a QR code
- `POST /api/qr/verify`
- Allowed roles: HOST or ADMIN
- Example body:
```json
{
  "qrContent": "...",
  "reservationId": 1
}
```

Use this at entry time to confirm the reservation is valid.

## 7. Host Active Reservations

HOST and ADMIN users can check who is currently parked in their spaces.

- `GET /reservations/active`

This returns only reservations that are currently active for the logged-in HOST's parking spaces.

## 8. Admin Dashboard and SMS Notifications

### Dashboard
- `GET /api/admin/dashboard`

This returns system totals like:
- number of parking spaces
- total slots
- active reservations
- bookings today
- revenue today
- occupancy percentage

### Send SMS
- `POST /api/ussd/sms`
- Allowed roles: ADMIN only
- Example body:
```json
{
  "phoneNumber": "+250788123456",
  "message": "Your reservation is almost ending."
}
```

Current behavior:
- the service logs the SMS in the server console
- to send real SMS, connect the method to the Africa's Talking SDK or REST API

## 9. USSD Flow

The public USSD endpoint is:
- `POST /api/ussd`

It expects Africa's Talking form data:
- `sessionId`
- `phoneNumber`
- `serviceCode`
- `text`

Example test payload:
```text
sessionId=abc123
phoneNumber=+250788123456
serviceCode=*384#
text=
```

Observed flow in the current code:
- blank `text` shows the main menu
- `1` shows nearby parking options
- `1*1`, `1*2`, or `1*3` move to slot selection
- a later confirmation step completes the booking message
- `2` shows a reservation-history placeholder

Important limitation:
- the USSD menu is currently hardcoded for MVP use
- it does not yet pull live parking data from the database

## 10. Recommended Swagger Test Order

1. Register a HOST account.
2. Register a DRIVER account.
3. Log in as HOST and create a parking space.
4. Log in as DRIVER and create a reservation for that space.
5. Initiate payment for the reservation.
6. Send a valid Flutterwave webhook to mark the reservation as paid.
7. Generate the QR code for the paid reservation.
8. Verify the QR as HOST or ADMIN.
9. Cancel a reservation to confirm slot restoration.
10. Log in as ADMIN and check the dashboard and SMS endpoint.
11. Test the USSD callback with form data.

## 11. Quick Troubleshooting

If login works but protected endpoints return `403`:
- click `Authorize` in Swagger and paste the Bearer token
- make sure you are using the correct role for that endpoint

If payment initiation fails:
- confirm `FLUTTERWAVE_SECRET_KEY` is set
- confirm `app.flutterwave.secret.key` is available at runtime
- confirm the frontend URL points to a valid payment callback page

If webhook calls are rejected:
- confirm the `verif-hash` header matches `FLUTTERWAVE_SECRET_HASH`
- make sure the webhook body contains a successful transaction event

If SMS only appears in logs:
- that is expected with the current MVP implementation
- replace `NotificationService.sendSms(...)` with the real Africa's Talking integration when ready

If USSD is not reaching your backend:
- make sure your Africa's Talking callback URL points to `/api/ussd`
- the endpoint must receive form-encoded data, not JSON

## 12. One-Line End-to-End Summary

Driver or HOST registers, logs in, authorizes Swagger, HOST creates a parking space, DRIVER creates a reservation, DRIVER initiates payment, Flutterwave webhook marks it paid, QR is generated and verified, DRIVER can cancel if needed, ADMIN can inspect the dashboard and send SMS, and USSD users can book through the callback flow.