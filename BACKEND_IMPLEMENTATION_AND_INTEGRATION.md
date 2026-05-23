# ParkShare Kigali Backend Implementation Notes

Base API: `https://smart-parking-backend-2.onrender.com`

This file records the backend gaps that were implemented in this pass and the integration points the frontend can now rely on.

## What was added

### Auth and account recovery

- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `PUT /api/auth/me`
- `PATCH /api/auth/me/password`
- `PATCH /api/auth/me/notifications`
- `PATCH /api/auth/me/preferences`

These endpoints extend the existing login/register/session flow without changing token issuance or role checks.

### Admin staff management

- `GET /api/admin/staff`
- `POST /api/admin/staff`
- `PATCH /api/admin/staff/{id}`

This is a lightweight staff registry for the admin console. It does not change the core DRIVER/HOST/ADMIN login roles.

### Event and parking linking

- `GET /events` for admins
- `POST /events/{eventId}/parking-spaces`
- `PATCH /parking-spaces/{identifier}/event-mode`

This lets the frontend publish platform events, link spaces to an event, and toggle event mode on a specific lot.

### USSD callback support

- `POST /api/ussd`
- `POST /api/ussd/session`
- `POST /api/ussd/sms`

The SMS endpoint is now reachable through security because `/api/ussd/**` is permitted.

## What the frontend can integrate now

- Password recovery screens can call `forgot-password` and `reset-password`.
- Profile settings screens can update names, phones, and notification preferences.
- Admin views can load staff rows from `GET /api/admin/staff` and edit them.
- Event admin views can list all events and link spaces directly.
- Host dashboards can toggle event mode per lot through the new parking endpoint.
- USSD callback flows can be tested locally or through Africa's Talking using the configured short codes.

## What still needs backend work

- Real email delivery for password reset links.
- Multipart parking-space photo upload.
- Provider-backed notification delivery instead of log-only SMS.
- Telco-specific USSD analytics and persistence.
- A dedicated webhook audit trail for payment callbacks.

## Response shape notes

- Existing endpoints continue to use `ApiResponse<T>` where they already did.
- The new endpoints follow the same pattern to keep the frontend parsing simple.
- `GET /api/auth/me` now returns the saved notification and preference fields too.

## How to verify quickly

1. Start the backend locally.
2. Log in as a DRIVER and call `/api/auth/me`, `/api/auth/me/password`, and `/api/auth/me/preferences`.
3. Log in as an ADMIN and call `/api/admin/staff` and `GET /events`.
4. Log in as a HOST and call `PATCH /parking-spaces/{identifier}/event-mode`.
5. POST a form request to `/api/ussd` with one of the configured short codes.