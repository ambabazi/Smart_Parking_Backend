# Curl examples for Smart Parking API

Base URL: https://smart-parking-api-xxxxx.onrender.com (replace with your deployed URL)

1) Register

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice Demo","email":"alice@example.com","phone":"+2507XXXXXXXX","password":"Password123","role":"DRIVER"}'
```

2) Login

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Password123"}'
```

3) Get profile (requires token from login)

```bash
curl https://smart-parking-api-xxxxx.onrender.com/api/auth/me \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

4) Refresh token

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'
```

5) List nearby parking

```bash
curl "https://smart-parking-api-xxxxx.onrender.com/parking-spaces/nearby?lat=-1.95&lng=30.06&radius=2000&page=0&size=10"
```

6) Create parking (HOST)

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/parking-spaces \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <HOST_JWT>" \
  -d '{"name":"Demo Lot","address":"Kacyiru","latitude":-1.95,"longitude":30.06,"totalSlots":20,"pricePerSlot":500}'
```

7) Get my parking (HOST)

```bash
curl https://smart-parking-api-xxxxx.onrender.com/parking-spaces/mine?page=0&size=10 \
  -H "Authorization: Bearer <HOST_JWT>"
```

8) Create reservation (DRIVER)

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <DRIVER_JWT>" \
  -d '{"parkingSpaceId":1,"slotCount":1,"startTime":"2026-05-15T10:00:00Z","endTime":"2026-05-15T12:00:00Z"}'
```

9) Verify QR (HOST)

```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/api/qr/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <HOST_JWT>" \
  -d '{"reservationId":123,"qrContent":"<QR_PAYLOAD>"}'
```

10) Actuator health

```bash
curl https://smart-parking-api-xxxxx.onrender.com/actuator/health
```
