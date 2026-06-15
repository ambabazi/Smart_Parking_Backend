# Smart Parking — Frontend Integration Guide

This is the authoritative guide for integrating a web/mobile frontend with the
Smart Parking backend. It covers base URLs, the auth flow, response conventions,
every endpoint with its request/response shape, a ready-to-use API client, and
TypeScript types.

> For interactive exploration, the backend also serves **Swagger UI** at
> `/swagger-ui.html` and the **OpenAPI spec** at `/api-docs`.

---

## 1. Base URL & environments

| Environment | Base URL |
|-------------|----------|
| Local       | `http://localhost:8080` |
| Production (Render) | `https://<your-service>.onrender.com` |

There is **no global `/api` prefix** — some routes start with `/api/...` and some
do not (see each section). Always build paths exactly as documented.

Recommended frontend config:

```bash
# .env (Vite example)
VITE_API_BASE_URL=http://localhost:8080
```

---

## 2. CORS

CORS is preconfigured on the backend. The following origins are allowed out of the box:

- `http://localhost:*` and `http://127.0.0.1:*` (any port — local dev)
- `https://*.vercel.app` (Vercel previews/production)
- `https://*.onrender.com`

To add custom domains, set the `ALLOWED_ORIGINS` env var on the backend
(comma-separated). Example: `ALLOWED_ORIGINS=https://app.smartparking.rw,https://admin.smartparking.rw`.

> **Important:** `allowCredentials` is `false`. Do **not** rely on cookies. Send the
> JWT in the `Authorization` header (see below). The only exposed response header is
> `Authorization`.

---

## 3. Authentication & authorization

Auth is **stateless JWT**. There are two tokens:

- **Access token** (`token`) — short-lived; send it on every authenticated request.
- **Refresh token** (`refreshToken`) — long-lived; used to obtain a new access token.

Send the access token as a Bearer header:

```
Authorization: Bearer <access-token>
```

### Roles

Every user has exactly one role. Endpoints are gated by role.

| Role | Description |
|------|-------------|
| `DRIVER` | Books and manages reservations (default on register) |
| `HOST` | Owns parking spaces, sees their reservations, verifies QR codes |
| `ADMIN` | Manages events, staff, sees system-wide dashboard |

### Recommended token handling

1. On login/register, store `token` and `refreshToken` (e.g. `localStorage` or secure storage).
2. Attach `Authorization: Bearer <token>` to all requests.
3. On a `401`, call `POST /api/auth/refresh` with the refresh token to get a new pair,
   then retry the original request once. If refresh fails, log the user out.
4. On logout, call `POST /api/auth/logout` to revoke the refresh token server-side.

`accessTokenExpiresInMs` / `refreshTokenExpiresInMs` are returned on login so you can
schedule a proactive refresh before expiry.

### 3a. Google sign-in (OAuth2)

Google login uses a **server-side redirect** flow and hands your app's normal JWTs
back to the frontend. The frontend never talks to Google directly — it only kicks off
and finishes the flow.

> Google login is **opt-in** on the backend. It is only active when the deploy has
> `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID` /
> `..._CLIENT_SECRET` set (see the "Backend setup" note below). If they're unset,
> `/oauth2/authorization/google` returns `404` and you should hide the Google button.

**Flow**

1. The user clicks "Continue with Google". Do a **full-page navigation** (not `fetch`)
   to the backend:

   ```
   window.location.href = `${API_BASE}/oauth2/authorization/google`;
   ```

   > `API_BASE` here is the backend origin **without** the `/api` suffix, e.g.
   > `https://your-backend.onrender.com`. The path is `/oauth2/authorization/google`.

2. The backend redirects to Google, the user consents, Google redirects back to the
   backend (`/login/oauth2/code/google`), and the backend finds-or-creates the user.

3. The backend redirects the browser to your frontend callback route with the tokens
   as query params:

   ```
   {APP_FRONTEND_URL}/oauth/callback?token=<jwt>&refreshToken=<rt>&expiresIn=<ms>&refreshExpiresIn=<ms>
   ```

   On failure it redirects to the same path with `?error=<reason>` instead.

4. Implement a `/oauth/callback` route in your SPA that reads the query params, stores
   the tokens exactly like a normal login, then redirects into the app:

   ```ts
   // Route: /oauth/callback
   const params = new URLSearchParams(window.location.search);
   const error = params.get("error");
   if (error) {
     // show a toast, send user back to /login
   } else {
     const token = params.get("token")!;
     const refreshToken = params.get("refreshToken") || "";
     localStorage.setItem("token", token);
     localStorage.setItem("refreshToken", refreshToken);
     // Optionally call GET /api/auth/me to load the profile, then:
     window.history.replaceState({}, "", "/"); // strip tokens from the URL
     // navigate to your authenticated home
   }
   ```

   > After reading them, **strip the tokens from the URL** (as above) so they don't
   > linger in history/logs.

**Notes**

- Google users are created with role `DRIVER` and **no phone number**. Prompt them to
  complete their phone via `PUT /api/auth/profile` if your flow needs it.
- An existing email/password account with the same email is **linked** automatically
  (Google emails are verified), so the user keeps one account either way.
- `APP_FRONTEND_URL` is a backend env var (defaults to `http://localhost:5173`); it
  must point at your deployed frontend in production, and your `/oauth/callback` route
  must exist there.

**Backend setup (one-time, ops):** set these env vars on the backend service to enable it:

```
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=<client-id>.apps.googleusercontent.com
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=<client-secret>
APP_FRONTEND_URL=https://your-frontend.example.com
```

In the **Google Cloud Console** → *APIs & Services → Credentials → OAuth client (Web application)*:

- **Authorized redirect URIs** must include the backend callback:
  - `https://your-backend.onrender.com/login/oauth2/code/google`
  - `http://localhost:8080/login/oauth2/code/google` (local dev)
- **Authorized JavaScript origins**: your frontend origins (e.g. `http://localhost:5173`,
  `https://your-frontend.example.com`).
- Configure the **OAuth consent screen** (app name, support email, scopes
  `openid`, `email`, `profile`). While the app is in *Testing* mode, add testers under
  **Audience → Test users**, or Google returns `403 access_denied`.

---

## 4. Response conventions

### 4a. The `ApiResponse` envelope

**Most** endpoints wrap their payload in this envelope:

```json
{
  "success": true,
  "message": "Human-readable message",
  "data": { /* payload, or null */ }
}
```

On error, `success` is `false`, `message` describes the problem, and `data` is usually `null`.

> ⚠️ **Not every endpoint is wrapped.** A few return the raw object/array directly
> (no `success`/`message`/`data`). Each endpoint below is marked **Wrapped** or **Raw**.
> The notable raw ones are: all public `GET /parking-spaces*`, all `/events*`, and
> `/payments/*`.

### 4b. Pagination (Spring `Page`)

Paginated endpoints accept `?page=0&size=10&sort=field,asc` and return a Spring `Page`
object. When wrapped, the `Page` is inside `data`; when raw, the `Page` is the body.

```json
{
  "content": [ /* array of items */ ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "pageable": { "pageNumber": 0, "pageSize": 10 }
}
```

### 4c. Dates

All timestamps are ISO-8601 **without timezone** (`LocalDateTime`), e.g.
`2026-05-13T13:00:00`. Send the same format in request bodies.

### 4d. Entity identifiers

Anywhere a path takes an `{identifier}` (reservations, parking spaces, events), you
may pass **any** of: numeric `id`, the `uuid`, or the `referenceCode`. Prefer the
`uuid` or `referenceCode` in the UI to avoid exposing sequential IDs.

### 4e. HTTP status codes

| Status | Meaning |
|--------|---------|
| `200` / `201` | Success |
| `204` | Success, no body (e.g. no current reservation, event deactivated) |
| `400` | Validation error / bad request |
| `401` | Missing/invalid/expired token |
| `403` | Authenticated but wrong role, or resource not yours |
| `404` | Not found |
| `500` | Server error |

---

## 5. Endpoint reference

Legend: 🔓 = public, a role name (e.g. `DRIVER`) = requires that role, 🔐 = any authenticated user.

### 5.1 Auth — `/api/auth`

| Method | Path | Access | Body | Returns |
|--------|------|--------|------|---------|
| POST | `/api/auth/register` | 🔓 | `RegisterRequest` | Wrapped `AuthResponse` |
| POST | `/api/auth/login` | 🔓 | `LoginRequest` | Wrapped `AuthResponse` |
| POST | `/api/auth/refresh` | 🔓 | `{ refreshToken }` | Wrapped `AuthResponse` |
| POST | `/api/auth/logout` | 🔓 | `{ refreshToken }` | Wrapped `null` |
| POST | `/api/auth/forgot-password` | 🔓 | `{ email }` | Wrapped `null` |
| POST | `/api/auth/reset-password` | 🔓 | `{ token, newPassword }` | Wrapped `null` |
| GET  | `/oauth2/authorization/google` | 🔓 | — | `302` redirect (start Google login; see §3a) |
| GET  | `/login/oauth2/code/google` | 🔓 | — | `302` to `{frontend}/oauth/callback?token=…` (Google callback — handled by backend) |
| GET  | `/api/auth/me` | 🔐 | — | Wrapped `UserProfileDTO` |
| PUT  | `/api/auth/me` | 🔐 | `{ fullName, phone }` | Wrapped `UserProfileDTO` |
| PATCH | `/api/auth/me/password` | 🔐 | `{ currentPassword, newPassword }` | Wrapped `null` |
| PATCH | `/api/auth/me/notifications` | 🔐 | `UpdatePreferencesRequest` | Wrapped `NotificationPreferencesDTO` |
| PATCH | `/api/auth/me/preferences` | 🔐 | `UpdatePreferencesRequest` | Wrapped `NotificationPreferencesDTO` |

**`RegisterRequest`**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+250788123456",
  "password": "at-least-8-chars",
  "role": "DRIVER"
}
```
- `phone` must match `+250XXXXXXXXX` (Rwanda format).
- `password` minimum 8 characters.
- `role` optional, defaults to `DRIVER`. Pass `HOST` or `ADMIN` as needed.

**`AuthResponse`** (inside `data`)
```json
{
  "token": "<jwt-access-token>",
  "refreshToken": "<jwt-refresh-token>",
  "type": "Bearer",
  "userId": 1,
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "role": "DRIVER",
  "accessTokenExpiresInMs": 900000,
  "refreshTokenExpiresInMs": 604800000
}
```

**`UserProfileDTO`**
```json
{
  "id": 1,
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+250788123456",
  "role": "DRIVER",
  "notificationsEnabled": true,
  "emailNotificationsEnabled": true,
  "smsNotificationsEnabled": true,
  "preferredLanguage": "en",
  "reminderMinutesBeforeEnd": 10
}
```

---

### 5.2 Parking spaces — `/parking-spaces`

**Public (Raw responses):**

| Method | Path | Access | Returns |
|--------|------|--------|---------|
| GET | `/parking-spaces` | 🔓 | Raw `Page<ParkingDTO>` |
| GET | `/parking-spaces/{id}` | 🔓 | Raw `ParkingDTO` (id/uuid/referenceCode) |
| GET | `/parking-spaces/by-name/{name}` | 🔓 | Raw `ParkingDTO` |
| GET | `/parking-spaces/search?name=...` | 🔓 | Raw `Page<ParkingDTO>` |
| GET | `/parking-spaces/nearby?lat=&lng=&radius=` | 🔓 | Raw `Page<ParkingDTO>` |
| GET | `/parking-spaces/event/{eventIdentifier}` | 🔓 | Raw `Page<ParkingDTO>` |

`nearby` query params: `lat` (Double, required), `lng` (Double, required),
`radius` (Double, metres, default `2000`). Results are cached ~60s.

**HOST management (Wrapped responses):**

| Method | Path | Access | Body | Returns |
|--------|------|--------|------|---------|
| POST | `/parking-spaces` | HOST | `ParkingSpaceCreateDTO` | Wrapped `ParkingSpaceDetailDTO` (201) |
| GET | `/parking-spaces/mine` | HOST | — | Wrapped `Page<ParkingSpaceDetailDTO>` |
| PUT | `/parking-spaces/{id}` | HOST | `ParkingSpaceCreateDTO` | Wrapped `ParkingSpaceDetailDTO` |
| DELETE | `/parking-spaces/{id}` | HOST | — | Wrapped `null` |
| PATCH | `/parking-spaces/{identifier}/event-mode` | HOST/ADMIN | `{ eventEnabled, linkedEventIdentifier? }` | Wrapped `ParkingSpaceDetailDTO` |

**`ParkingSpaceCreateDTO`**
```json
{
  "name": "Kacyiru Parking",
  "address": "KG 7 Ave, Kacyiru, Kigali",
  "latitude": -1.9536,
  "longitude": 30.0605,
  "totalSlots": 50,
  "pricePerSlot": 500.0,
  "imageUrl": "https://picsum.photos/seed/kacyiru/800/500"
}
```
- `name`, `latitude`, `longitude`, `totalSlots` (positive), `pricePerSlot` (positive) required. `address` and `imageUrl` optional.
- `imageUrl` is **any URL** — a hosted/CDN image, an Unsplash/picsum link, or a frontend `/public` asset path (e.g. `"/Container.png"`). There is no file-upload endpoint; the backend stores the URL only.

**`ParkingDTO`** (public view)
```json
{
  "id": 1, "uuid": "…", "referenceCode": "PKG-2026-06-15-AB12CD34",
  "name": "Kacyiru Parking", "address": "…",
  "latitude": -1.9536, "longitude": 30.0605,
  "totalSlots": 50, "availableSlots": 47, "pricePerSlot": 500.0,
  "imageUrl": "https://picsum.photos/seed/kacyiru/800/500",
  "eventEnabled": false
}
```

**`ParkingSpaceDetailDTO`** adds `imageUrl`, `ownerId` and `ownerName` to the above.

> The database is seeded with **7 mock Kigali parking spaces** (each with coordinates,
> slots, price and an `imageUrl`) so the map has live data immediately on a fresh deploy.

---

### 5.2a Rendering the map & parking-space photos

**Endpoint + shape (read this if your map shows demo data):** the parking list lives at
**`GET {API_BASE}/parking-spaces`** (note the `-spaces` suffix) and returns a Spring
`Page<ParkingDTO>` — the spots are in **`.content`**, and each spot uses
**`latitude`/`longitude`** (numbers), **`availableSlots`**, **`pricePerSlot`** and
**`imageUrl`**. Map any backend spot to your UI model like:

```ts
type ParkingDTO = {
  id: number; uuid: string; referenceCode: string;
  name: string; address?: string;
  latitude: number; longitude: number;
  totalSlots: number; availableSlots: number; pricePerSlot: number;
  imageUrl?: string; eventEnabled: boolean;
};

const res = await fetch(`${API_BASE}/parking-spaces?size=100`);
const page = await res.json();
const spots = (page.content as ParkingDTO[]).map((s) => ({
  id: s.uuid,
  name: s.name,
  address: s.address,
  location: { lat: s.latitude, lng: s.longitude },
  availability: s.availableSlots,
  totalSlots: s.totalSlots,
  price: s.pricePerSlot,
  image: s.imageUrl,            // <-- show this in the popup/detail panel
}));
```

Render `image` in the marker popup / detail card, e.g.
`{spot.image && <img src={spot.image} alt={spot.name} className="h-28 w-full rounded-lg object-cover" />}`.

**Switching the map to Google Maps + deeper zoom.** Install the loader, add the API key
to the frontend env, and allow a higher `maxZoom` for street-level detail:

```bash
npm install @vis.gl/react-google-maps
```

```env
# frontend .env
VITE_GOOGLE_MAPS_API_KEY=AIza...your-browser-key
VITE_API_BASE_URL=https://your-backend.onrender.com
```

```tsx
import { APIProvider, Map, AdvancedMarker, InfoWindow } from '@vis.gl/react-google-maps';

<APIProvider apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY}>
  <Map
    defaultCenter={{ lat: -1.9441, lng: 30.0619 }}
    defaultZoom={13}
    maxZoom={20}          {/* deeper street-level zoom (Leaflet was capped at 17) */}
    mapId="YOUR_MAP_ID"
    gestureHandling="greedy"
  >
    {spots.map((s) => (
      <AdvancedMarker key={s.id} position={s.location} onClick={() => setSelected(s)} />
    ))}
  </Map>
</APIProvider>
```

To "zoom deeper" with the existing **Leaflet** map instead (no API key), just raise the cap:
change `maxZoom={17}` to `maxZoom={20}` on `<MapContainer>` and use a tile source that
serves zoom 18–20 tiles.

**Google Console for Maps (separate from OAuth login):** enable the **"Maps JavaScript API"**,
create an **API key** (Credentials → Create credentials → API key), restrict it to your
frontend domains under **Application restrictions → HTTP referrers**, restrict it to the
Maps JavaScript API under **API restrictions**, and **enable billing** on the project
(Google Maps requires a billing account even within the free monthly credit).

---

### 5.3 Reservations — `/reservations`

| Method | Path | Access | Body/Params | Returns |
|--------|------|--------|-------------|---------|
| POST | `/reservations` | DRIVER | `BookingRequest` | Wrapped `ReservationResponseDTO` |
| GET | `/reservations/my` | DRIVER | `?page=&size=` | Wrapped `Page<ReservationResponseDTO>` |
| GET | `/reservations/check-availability` | 🔐 | `?parkingSpaceId=&startTime=&endTime=&slotCount=` | Wrapped `AvailabilityResponse` |
| GET | `/reservations/active/current` | DRIVER | — | Wrapped `CurrentReservationDTO` or `204` |
| GET | `/reservations/{identifier}` | 🔐 | — | Wrapped `ReservationResponseDTO` |
| PATCH | `/reservations/{identifier}/cancel` | DRIVER | — | Wrapped `ReservationResponseDTO` |
| GET | `/reservations/active` | HOST/ADMIN | `?page=&size=` | Wrapped `Page<ReservationResponseDTO>` |
| POST | `/reservations/{identifier}/check-in` | DRIVER | — | Wrapped `ReservationResponseDTO` |
| POST | `/reservations/{identifier}/checkout` | DRIVER | — | Wrapped `CheckoutResponse` |
| POST | `/reservations/{identifier}/pay-overtime?amount=` | DRIVER | `amount` (positive) | Wrapped `ReservationResponseDTO` |

**`BookingRequest`**
```json
{
  "parkingSpaceId": "PKG-2026-06-15-AB12CD34",
  "slotCount": 1,
  "startTime": "2026-05-13T13:00:00",
  "endTime": "2026-05-13T15:00:00"
}
```
`parkingSpaceId` accepts numeric id, uuid, or referenceCode.

**`ReservationResponseDTO`**
```json
{
  "id": 10, "uuid": "…", "referenceCode": "RES-2026-06-15-XY98ZT76",
  "userId": 1, "userFullName": "Jane Doe", "userEmail": "jane@example.com",
  "parkingSpaceReferenceCode": "PKG-…", "parkingSpaceName": "Kacyiru Parking",
  "slotCount": 1,
  "startTime": "2026-05-13T13:00:00", "endTime": "2026-05-13T15:00:00",
  "totalAmount": 1000.00, "paid": false, "verified": false,
  "qrCode": "…", "createdAt": "2026-05-13T12:30:00"
}
```

**`AvailabilityResponse`**
```json
{ "available": true, "availableSlots": 47, "estimatedPrice": 1000.0, "pricePerHour": 500.0, "estimatedHours": 2 }
```

**`CheckoutResponse`**
```json
{
  "reservationReferenceCode": "RES-…",
  "checkedOutAt": "2026-05-13T15:10:00",
  "bookedUntil": "2026-05-13T15:00:00",
  "hasOvertime": true,
  "overtimeMinutes": 10,
  "overtimeAmount": 100.00,
  "message": "Checked out with overtime"
}
```

---

### 5.4 Events — `/events` (Raw responses)

| Method | Path | Access | Body | Returns |
|--------|------|--------|------|---------|
| POST | `/events` | ADMIN | `EventRequest` | Raw `EventResponse` (201) |
| GET | `/events/active` | 🔓 | — | Raw `Page<EventResponse>` |
| GET | `/events` | ADMIN | `?page=&size=` | Raw `Page<EventResponse>` |
| POST | `/events/{identifier}/parking-spaces` | ADMIN | `{ parkingSpaceIds: [..] }` | Raw `EventResponse` |
| DELETE | `/events/{identifier}/deactivate` | ADMIN | — | `204` |

**`EventRequest`**
```json
{
  "name": "BK Arena Concert",
  "latitude": -1.9500,
  "longitude": 30.1000,
  "radiusMetres": 1500,
  "startTime": "2026-05-15T18:00:00",
  "endTime": "2026-05-15T23:00:00"
}
```

**`EventResponse`**
```json
{
  "id": 1, "name": "BK Arena Concert",
  "latitude": -1.95, "longitude": 30.10, "radiusMetres": 1500,
  "startTime": "2026-05-15T18:00:00", "endTime": "2026-05-15T23:00:00",
  "activatedSpacesCount": 4
}
```

---

### 5.5 QR codes — `/api/qr`

| Method | Path | Access | Params/Body | Returns |
|--------|------|--------|-------------|---------|
| GET | `/api/qr/generate?reservationId=` | DRIVER | `reservationId` (id/uuid/code) | **PNG image bytes** (`image/png`) |
| POST | `/api/qr/verify` | HOST/ADMIN | `{ qrContent, reservationId }` | Wrapped `QRVerificationResponse` |

- `generate` returns a binary PNG (not JSON). The user is derived from the token —
  drivers can only generate QR for their own reservations. Render it via a blob:

```ts
const res = await fetch(`${BASE}/api/qr/generate?reservationId=${id}`, {
  headers: { Authorization: `Bearer ${token}` },
});
const blob = await res.blob();
imgEl.src = URL.createObjectURL(blob);
```

**`QRVerificationResponse`** (`status` is one of `VALID`, `INVALID`, `ALREADY_USED`,
`EXPIRED`, `NOT_STARTED`):
```json
{
  "status": "VALID",
  "valid": true,
  "driverName": "Jane Doe",
  "licensePlate": null,
  "reservation": { "id": 10, "startTime": "…", "endTime": "…", "parkingSpaceName": "…", "totalAmount": 1000.00 }
}
```
A successful `VALID` verify also checks the driver in (sets status `CHECKED_IN`).

---

### 5.6 Payments — `/payments` (Raw responses)

| Method | Path | Access | Returns |
|--------|------|--------|---------|
| POST | `/payments/initiate/{reservationIdentifier}` | DRIVER | `{ paymentUrl, reservationReferenceCode }` |
| GET | `/payments/status/{reservationIdentifier}` | DRIVER | `PaymentStatusDTO` or `204` |
| POST | `/payments/webhook` | server-to-server | `200` (Flutterwave only) |

**Flow:** call `initiate` → redirect the user to `paymentUrl` (Flutterwave checkout)
→ Flutterwave calls the backend `webhook` to confirm → poll `GET /payments/status/...`
or refetch the reservation to see `paid: true`.

`PaymentStatusDTO`: `{ "amount": 1000.00, "transactionId": "…" }` (plus status fields).
The `webhook` endpoint is for Flutterwave only (verified by `verif-hash`); the frontend never calls it.

---

### 5.7 Dashboards

| Method | Path | Access | Returns |
|--------|------|--------|---------|
| GET | `/api/admin/dashboard` | ADMIN | Wrapped `DashboardDTO` |
| GET | `/api/owner/dashboard` | HOST/ADMIN | Wrapped `OwnerDashboardDTO` |
| GET | `/api/driver/dashboard` | DRIVER | Wrapped `DriverDashboardDTO` |

**`DashboardDTO`** (admin): `totalParkingSpaces`, `totalReservationSlots`,
`activeReservations`, `bookingsToday`, `revenueToday`, `occupancyPercentage`.

**`OwnerDashboardDTO`**: `totalParkingSpaces`, `totalSlots`, `availableSlots`,
`occupiedSlots`, `occupancyPercentage`, `activeReservations`, `totalReservations`,
`totalRevenue`, `revenueToday`, `memberSince`.

**`DriverDashboardDTO`**: `totalReservations`, `completedReservations`,
`activeReservations`, `cancelledReservations`, `upcomingReservations`, `totalSpent`,
`membersince`.

---

### 5.8 Admin staff — `/api/admin/staff` (ADMIN, Wrapped)

| Method | Path | Body | Returns |
|--------|------|------|---------|
| GET | `/api/admin/staff?staffRole=&page=&size=&sort=&direction=` | — | Wrapped `Page<AdminStaffResponse>` |
| POST | `/api/admin/staff` | `AdminStaffRequest` | Wrapped `AdminStaffResponse` (201) |
| PATCH | `/api/admin/staff/{id}` | `AdminStaffRequest` | Wrapped `AdminStaffResponse` |

`AdminStaffResponse`: `{ id, email, fullName, phone, staffRole, active, createdAt }`.

---

### 5.9 Notifications / USSD — `/api/ussd`

| Method | Path | Access | Notes |
|--------|------|--------|-------|
| POST | `/api/ussd/sms` | ADMIN | `{ phoneNumber, message }` → Wrapped |
| POST | `/api/ussd` , `/api/ussd/session` | telco only | Africa's Talking callback (form-urlencoded). Not for frontend. |

---

### 5.10 Health

| Method | Path | Returns |
|--------|------|---------|
| GET | `/ping` | `pong` (use this for uptime/health checks) |
| GET | `/health`, `/` | 302 redirect to Swagger UI |

> If configuring a Render **Health Check Path**, use `/ping` — not `/actuator/health`
> (that path requires auth and returns `403`).

---

## 6. Drop-in API client (TypeScript)

A minimal client that handles the envelope, auth header, and automatic refresh:

```ts
const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

let accessToken: string | null = localStorage.getItem("token");
let refreshToken: string | null = localStorage.getItem("refreshToken");

function setTokens(token: string, refresh: string) {
  accessToken = token;
  refreshToken = refresh;
  localStorage.setItem("token", token);
  localStorage.setItem("refreshToken", refresh);
}

function clearTokens() {
  accessToken = refreshToken = null;
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
}

async function refresh(): Promise<boolean> {
  if (!refreshToken) return false;
  const res = await fetch(`${BASE}/api/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!res.ok) return false;
  const body = await res.json();
  setTokens(body.data.token, body.data.refreshToken);
  return true;
}

export async function api<T = unknown>(
  path: string,
  options: RequestInit = {},
  retry = true
): Promise<T> {
  const headers = new Headers(options.headers);
  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const res = await fetch(`${BASE}${path}`, { ...options, headers });

  if (res.status === 401 && retry && (await refresh())) {
    return api<T>(path, options, false); // retry once after refresh
  }
  if (res.status === 401) {
    clearTokens();
    throw new Error("Session expired");
  }
  if (res.status === 204) return undefined as T;

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data?.message ?? `Request failed (${res.status})`);
  }
  return data as T;
}

// Example usage
export async function login(email: string, password: string) {
  const body = await api<{ data: AuthResponse }>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  setTokens(body.data.token, body.data.refreshToken);
  return body.data;
}

export const getNearby = (lat: number, lng: number, radius = 2000) =>
  api<Page<ParkingDTO>>(`/parking-spaces/nearby?lat=${lat}&lng=${lng}&radius=${radius}`);

export const getMyReservations = (page = 0, size = 10) =>
  api<ApiResponse<Page<ReservationResponseDTO>>>(`/reservations/my?page=${page}&size=${size}`);
```

> Remember: **raw** endpoints (e.g. `/parking-spaces/nearby`, `/events/active`) return
> the object directly, while **wrapped** endpoints return `{ success, message, data }`.
> Type the call accordingly (`Page<...>` vs `ApiResponse<Page<...>>`).

---

## 7. TypeScript types

```ts
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export type Role = "DRIVER" | "HOST" | "ADMIN";

export interface AuthResponse {
  token: string;
  refreshToken: string;
  type: "Bearer";
  userId: number;
  fullName: string;
  email: string;
  role: Role;
  accessTokenExpiresInMs: number;
  refreshTokenExpiresInMs: number;
}

export interface UserProfileDTO {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: Role;
  notificationsEnabled: boolean;
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  preferredLanguage: string;
  reminderMinutesBeforeEnd: number;
}

export interface ParkingDTO {
  id: number;
  uuid: string;
  referenceCode: string;
  name: string;
  address: string | null;
  latitude: number;
  longitude: number;
  totalSlots: number;
  availableSlots: number;
  pricePerSlot: number;
  imageUrl: string | null;
  eventEnabled: boolean;
}

export interface ParkingSpaceDetailDTO extends ParkingDTO {
  ownerId: number;
  ownerName: string;
}

export interface BookingRequest {
  parkingSpaceId: string; // id | uuid | referenceCode
  slotCount: number;
  startTime: string; // ISO LocalDateTime, e.g. "2026-05-13T13:00:00"
  endTime: string;
}

export interface ReservationResponseDTO {
  id: number;
  uuid: string;
  referenceCode: string;
  userId: number;
  userFullName: string;
  userEmail: string;
  parkingSpaceReferenceCode: string;
  parkingSpaceName: string;
  slotCount: number;
  startTime: string;
  endTime: string;
  totalAmount: number;
  paid: boolean;
  verified: boolean;
  qrCode: string;
  createdAt: string;
}

export interface AvailabilityResponse {
  available: boolean;
  availableSlots: number;
  estimatedPrice: number;
  pricePerHour: number;
  estimatedHours: number;
}

export interface CheckoutResponse {
  reservationReferenceCode: string;
  checkedOutAt: string;
  bookedUntil: string;
  hasOvertime: boolean;
  overtimeMinutes: number;
  overtimeAmount: number | null;
  message: string;
}

export interface EventResponse {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  radiusMetres: number;
  startTime: string;
  endTime: string;
  activatedSpacesCount: number;
}

export type QrStatus = "VALID" | "INVALID" | "ALREADY_USED" | "EXPIRED" | "NOT_STARTED";

export interface QRVerificationResponse {
  status: QrStatus;
  valid: boolean;
  driverName: string | null;
  licensePlate: string | null;
  reservation: {
    id: number;
    startTime: string;
    endTime: string;
    parkingSpaceName: string;
    totalAmount: number;
  } | null;
}

export interface DashboardDTO {
  totalParkingSpaces: number;
  totalReservationSlots: number;
  activeReservations: number;
  bookingsToday: number;
  revenueToday: number;
  occupancyPercentage: number;
}

export interface OwnerDashboardDTO {
  totalParkingSpaces: number;
  totalSlots: number;
  availableSlots: number;
  occupiedSlots: number;
  occupancyPercentage: number;
  activeReservations: number;
  totalReservations: number;
  totalRevenue: number;
  revenueToday: number;
  memberSince: string;
}

export interface DriverDashboardDTO {
  totalReservations: number;
  completedReservations: number;
  activeReservations: number;
  cancelledReservations: number;
  upcomingReservations: number;
  totalSpent: number;
  membersince: string;
}
```

---

## 8. End-to-end flows

### Driver: find → book → pay → check in → check out
1. `GET /parking-spaces/nearby?lat=&lng=` → pick a space.
2. (optional) `GET /reservations/check-availability?...` → show price estimate.
3. `POST /reservations` → get reservation (`paid: false`).
4. `POST /payments/initiate/{ref}` → redirect to `paymentUrl`; backend confirms via webhook.
5. `GET /api/qr/generate?reservationId={ref}` → show QR (PNG).
6. At entry, HOST does `POST /api/qr/verify` (this checks the driver in).
7. On exit, `POST /reservations/{ref}/checkout`; if `hasOvertime`, `POST /reservations/{ref}/pay-overtime?amount=`.

### Host: register space → monitor → verify
1. Register/login as `HOST`.
2. `POST /parking-spaces` to list a space; `GET /parking-spaces/mine` to manage.
3. `GET /reservations/active` to see who's parked now; `GET /api/owner/dashboard` for KPIs.
4. `POST /api/qr/verify` to validate arriving drivers.

### Admin: events & oversight
1. Login as `ADMIN`.
2. `POST /events` (auto-activates nearby spaces); `GET /events` / `DELETE /events/{id}/deactivate`.
3. `GET /api/admin/dashboard`, manage `/api/admin/staff`.

---

## 9. Gotchas checklist

- [ ] Send `Authorization: Bearer <token>` — **not** cookies (`allowCredentials=false`).
- [ ] Handle the **wrapped vs raw** distinction per endpoint.
- [ ] Timestamps are `LocalDateTime` (no `Z`/offset).
- [ ] Use `uuid`/`referenceCode` in URLs instead of numeric IDs where possible.
- [ ] `GET /api/qr/generate` returns a **PNG**, not JSON — read it as a blob.
- [ ] Implement the 401 → refresh → retry-once loop.
- [ ] Add your deployed frontend origin to `ALLOWED_ORIGINS` if it isn't `*.vercel.app`/`*.onrender.com`.
```
