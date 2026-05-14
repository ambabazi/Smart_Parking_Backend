# BE1 Sprint Setup Complete ✓

## Smart Parking Kigali — Backend Engineer 1 Sprint Implementation

This document summarizes the complete **BE1 Sprint** implementation for Smart Parking Kigali MVP.

---

## 📋 What's Been Completed

### ✅ Sprint 1: Setup & Database
- [x] **pom.xml** — Updated with all required dependencies (JWT, QR, WebFlux, Swagger, Flyway)
- [x] **application.properties** — Environment-based configuration with Africa's Talking + JWT
- [x] **.env & .env.local** — Secure configuration templates (never commit real secrets)
- [x] **V1__init_schema.sql** — Complete Kigali database schema (users, parking_spaces, reservations, payments, events)
- [x] **V2__insert_demo_data.sql** — Seed data with 5 real Kigali parking spaces + admin user
- [x] **SmartParkingApplication.java** — Added `@EnableScheduling` for event management
- [x] **docker-compose.yml** — PostgreSQL container for local development

### ✅ Sprint 2: Auth, JWT & Swagger
- [x] **Role.java** — Enum: DRIVER, HOST, ADMIN
- [x] **User.java** — JPA entity implementing `UserDetails` for Spring Security
- [x] **UserRepository.java** — JPA repository with findByEmail, existsByPhone
- [x] **RegisterRequest.java** — Validation: phone format `+250XXXXXXXXX`, password 8+ chars
- [x] **LoginRequest.java** — Email + password login with validation
- [x] **AuthResponse.java** — Returns JWT token + user metadata (userId, fullName, role)
- [x] **JwtService.java** — Generate & validate JWT tokens (role embedded in payload)
- [x] **JwtAuthFilter.java** — Intercepts Authorization header, validates token
- [x] **UserDetailsServiceImpl.java** — Loads User by email from database
- [x] **SecurityConfig.java** — CSRF disabled, stateless sessions, public auth routes
- [x] **AppConfig.java** — CORS filter (allow all origins for development)
- [x] **SwaggerConfig.java** — Swagger UI with JWT Authorize button
- [x] **WebSocketConfig.java** — WebSocket support (future real-time features)
- [x] **AuthService.java** — Register & login logic with password encoding
- [x] **AuthController.java** — POST /api/auth/register, /api/auth/login endpoints
- [x] **Endpoints**:
  - `POST /api/auth/register` — Create account (email, phone, fullName, password, role)
  - `POST /api/auth/login` — Authenticate (email, password) → JWT token

### ✅ Sprint 3: QR, Common & Notifications
- [x] **ApiResponse.java** — Generic wrapper: `{success, message, data}`
- [x] **ResourceNotFoundException.java** — Custom exception for 404 errors
- [x] **GlobalExceptionHandler.java** — Centralized error handling (validation, not found, general)
- [x] **QrService.java** — Generate QR codes as PNG + verify QR content
  - `generateQrBase64()` — Base64 for database storage
  - `generateQrBytes()` — Raw PNG bytes for image response
  - `verifyQrContent()` — Validate QR format (reservationId|userId|timestamp)
- [x] **VerifyRequest.java** — {reservationId, qrContent}
- [x] **QrController.java** — Endpoints:
  - `GET /api/qr/generate?reservationId=X&userId=Y` → PNG image
  - `POST /api/qr/verify` → Verify QR for entry (HOST/ADMIN only)
- [x] **NotificationService.java** — SMS notifications (logs in MVP mode)
  - `sendBookingConfirmation()` — Booking confirmation SMS
  - `sendOvertimeWarning()` — Warning before timeout
  - `sendOvertimeCharge()` — Overtime fee notification
  - `notifyHost()` — Host notification when driver books
- [x] **UssdController.java** — USSD menu for non-smartphone users
  - `POST /api/ussd` — Public endpoint for Africa's Talking callbacks
  - Interactive menu: View parking → Select location → Book → Confirm

---

## 🚀 Local Development Setup

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 16 (via Docker)

### Step 1: Start PostgreSQL
```bash
docker-compose up -d
# Verify: docker ps
```

### Step 2: Create .env.local
Copy `.env` template and set your values:
```bash
cp .env .env.local
# Edit .env.local with actual secrets (not committed to git)
```

### Step 3: Run the Application
```bash
# Option A: Maven
mvn spring-boot:run

# Option B: Build & run JAR
mvn clean package
java -jar target/parking-0.0.1-SNAPSHOT.jar
```

### Step 4: Verify Application Started
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Step 5: Access Swagger UI
Open in browser:
```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Smoke Test Checklist

Run these before every deployment:

| # | Endpoint | Method | Expect | Status |
|---|----------|--------|--------|--------|
| 1 | /actuator/health | GET | 200, `{status: UP}` | ⬜ |
| 2 | /api/auth/register | POST | 201, token | ⬜ |
| 3 | /api/auth/login (valid) | POST | 200, token | ⬜ |
| 4 | /api/auth/login (wrong pw) | POST | 401, error | ⬜ |
| 5 | /api/qr/generate (no token) | GET | 403, Forbidden | ⬜ |
| 6 | Authorize in Swagger | - | Lock icon closed | ⬜ |
| 7 | /api/qr/generate (with token) | GET | 200, PNG image | ⬜ |
| 8 | /api/qr/verify (HOST) | POST | 200, QR valid | ⬜ |
| 9 | /swagger-ui.html | GET | 200, loads fully | ⬜ |
| 10 | /api-docs | GET | 200, JSON spec | ⬜ |

### Test Register & Login Flow
```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Driver",
    "email": "john@example.com",
    "phone": "+250788123456",
    "password": "SecurePass123",
    "role": "DRIVER"
  }'

# Copy the JWT token from response

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# 3. Generate QR with token
curl -X GET 'http://localhost:8080/api/qr/generate?reservationId=1&userId=1' \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output qr.png
```

---

## 🔑 Database Demo Users

Flyway automatically seeds these on startup:

### Admin User
- **Email**: `admin@smartparking.rw`
- **Password**: `Admin@123`
- **Role**: ADMIN

### Host User
- **Email**: `host@smartparking.rw`
- **Password**: `Admin@123`
- **Role**: HOST

### Kigali Parking Spaces (Pre-seeded)
1. **BK Arena Parking** — 80 slots, RWF 500/hr
2. **Kigali Convention Centre** — 120 slots, RWF 1000/hr
3. **Nyarugenge Market Parking** — 40 slots, RWF 300/hr
4. **Remera Parking Zone** — 30 slots, RWF 300/hr
5. **Kicukiro Commercial Parking** — 25 slots, RWF 250/hr

---

## 📦 Git Workflow

All work is on **feature/be1-sprint-complete** branch:

```bash
# View current branch
git branch -a

# Switch to BE1 branch
git checkout feature/be1-sprint-complete

# View commits
git log --oneline -10

# Merge to develop when ready for BE2
git checkout develop
git merge feature/be1-sprint-complete

# Push to main after team review
git checkout main
git merge develop
git push origin main
```

---

## 🌐 Africa's Talking Integration (Future)

For production SMS/USSD:

1. **Get Sandbox Key**: https://account.africastalking.com → Sandbox → API Key
2. **Update .env**:
   ```
   AT_USERNAME=your_username
   AT_API_KEY=your_api_key
   AT_SENDER_ID=SmartPark
   AT_ENV=sandbox
   ```
3. **Enable SDK**:
   - Uncomment Africa's Talking dependency in `pom.xml`
   - Update `NotificationService.java` to use `AfricasTalking.getService()`

Currently, notifications log to console (MVP mode).

---

## 🚢 Railway Deployment

### Prerequisites
- GitHub account (repo pushed)
- Railway account (free tier available)

### Step 1: Push to GitHub
```bash
# Ensure .env is in .gitignore
git add .
git commit -m "feat: be1-sprint-complete ready for deployment"
git push origin feature/be1-sprint-complete
```

### Step 2: Create Railway Project
1. Visit **railway.app** → New Project
2. Deploy from GitHub → Select **Smart_Parking** repository
3. Select **main** branch
4. Railway auto-detects Dockerfile

### Step 3: Add PostgreSQL Service
1. In Railway dashboard → **+ Add Service** → Database → PostgreSQL
2. Copy `DATABASE_URL` from PostgreSQL service variables

### Step 4: Set Environment Variables
In Railway project settings, add:
```
DB_URL=jdbc:postgresql://[HOST]:[PORT]/railway
DB_USERNAME=postgres
DB_PASSWORD=[from PostgreSQL service]
JWT_SECRET=[generate: openssl rand -hex 32]
PT_USERNAME=sandbox
AT_API_KEY=[your sandbox key]
AT_SENDER_ID=SmartPark
AT_ENV=sandbox
PORT=8080
```

### Step 5: Deploy & Test
```bash
# Wait for build to complete (~3 min)
# Then test:
curl https://your-url.up.railway.app/actuator/health

# Open Swagger:
https://your-url.up.railway.app/swagger-ui.html
```

### Step 6: Keep Railway Alive
Railway free tier sleeps after 30 min of inactivity.

Set up cron ping at **cron-job.org**:
- **URL**: `https://your-url.up.railway.app/actuator/health`
- **Schedule**: Every 5 minutes

---

## 🛠 Architecture Overview

```
┌─────────────────────────────────────────────┐
│  Frontend (Angular/React)                   │
│  http://localhost:3000                      │
└────────────────┬────────────────────────────┘
                 │
                 │ JWT Token
                 ▼
┌─────────────────────────────────────────────┐
│  Spring Boot Backend (Port 8080)            │
├─────────────────────────────────────────────┤
│  ✅ Auth Service (JWT, Register/Login)      │
│  ✅ QR Service (Generate, Verify)           │
│  ✅ Notification Service (SMS/USSD)         │
│  ✅ Security (JwtAuthFilter, UserDetails)   │
│  ✅ Swagger UI (Endpoint documentation)     │
└────────────────┬────────────────────────────┘
                 │
                 │ JDBC Driver
                 ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL 16 (Port 5432)                  │
│  Database: smart_parking                    │
│  Tables: users, parking_spaces,             │
│          reservations, payments, events     │
└─────────────────────────────────────────────┘
```

---

## 📣 Next Steps (BE2)

Once BE1 is merged to `main`, BE2 Engineer implements:

- **Reservation Service** — Create, update, cancel bookings
- **Payment Service** — Integrate Flutterwave for payment processing
- **Event Management** — Auto-deactivate events, pricing multiplier
- **Real-time Updates** — WebSocket notifications
- **Scheduled Tasks** — Overtime warnings, auto-extend features

BE2 will:
1. Pull from `main` (includes BE1 code)
2. Create `feature/be2-reservations` branch
3. Implement all reservation + payment logic
4. Merge back to `develop` for integration testing
5. Final merge to `main` for production

---

## ❓ Troubleshooting

### Port 8080 already in use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
server.port=9090
```

### Database connection refused
```bash
# Ensure Docker PostgreSQL is running
docker-compose ps

# Restart if needed
docker-compose restart postgres
```

### JWT validation fails
- Check `jwt.secret` length (minimum 32 characters)
- Verify token not expired (check `jwt.expiration`)
- Ensure `Authorization: Bearer <token>` format

### Flyway migration fails
- Check database schema permissions
- Verify `spring.flyway.baseline-on-migrate=true` is set
- Check migration file names: `V1__`, `V2__` (double underscore)

---

## 📚 Documentation Links

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **JWT (JJWT)**: https://github.com/jwtk/jjwt
- **Flyway Migrations**: https://flywaydb.org/documentation/
- **Swagger/OpenAPI**: https://springdoc.org/
- **Africa's Talking**: https://africastalking.com/sms/send
- **Railway**: https://docs.railway.app/

---

## 👥 Team Assignments

| Component | Owner | Status |
|-----------|-------|--------|
| Auth/JWT | ✅ BE1 | Complete |
| QR Codes | ✅ BE1 | Complete |
| Notifications | ✅ BE1 | Complete |
| Reservations | ⏳ BE2 | Pending |
| Payments | ⏳ BE2 | Pending |
| Real-time | ⏳ BE2 | Pending |
| DevOps/Deploy | ⏳ DevOps | Pending |

---

## 📝 License

Smart Parking Kigali © 2026
Built for Kigali Tech Hackathon MVP
