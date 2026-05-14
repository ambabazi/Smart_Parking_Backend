# Smart Parking First-Time Guide

This guide is for someone opening the app for the first time and trying to understand:
- what the app does
- how the code is organized
- how the pieces relate to each other
- how to run it
- how to use the real API endpoints in Swagger
- why the app is built this way

## 1. What This App Is

Smart Parking is a Spring Boot backend for a parking reservation system.

The main idea is simple:
- drivers register and log in
- drivers search for parking spaces
- drivers create reservations
- reservations can later be paid for
- QR codes can be generated and verified at entry
- a USSD flow exists for users who do not use the web app

This repository is the backend only. The frontend is not included here.

## 2. Big Picture

The app follows a standard backend structure:
- Controller: receives HTTP requests
- Service: contains business logic
- Repository: reads and writes the database
- Entity: represents database rows
- DTO: data sent back to the client
- Configuration: security, Swagger, database, and runtime setup

The request flow usually looks like this:
1. Swagger or a client sends an HTTP request.
2. A controller receives it.
3. The controller calls a service.
4. The service uses repositories to access the database.
5. The service returns data or throws an error.
6. The controller sends the response back.

## 3. Main Modules

### Authentication
Files in `src/main/java/com/smart/parking/auth`

This module handles:
- registration
- login
- JWT token generation
- user roles

Important files:
- `AuthController.java`
- `AuthService.java`
- `User.java`
- `UserRepository.java`
- `LoginRequest.java`
- `RegisterRequest.java`
- `AuthResponse.java`
- `Role.java`

What it does:
- creates a user account
- authenticates a user
- returns a JWT token
- stores the user role such as `DRIVER`, `HOST`, or `ADMIN`

Why it exists:
- the app is not using sessions
- it uses stateless authentication with JWT
- every protected request must include a Bearer token

### Parking
Files in `src/main/java/com/smart/parking/parking`

This module models parking spaces.

Important files:
- `ParkingSpace.java`
- `ParkingSpaceRepository.java`
- `ParkingService.java`
- `ParkingDTO.java`
- `ParkingController.java`

What it does:
- stores parking space data such as name, GPS coordinates, price, and free slots
- supports nearby search using latitude and longitude
- can return a lightweight DTO instead of the full entity

Important note:
- `ParkingController.java` is currently empty
- the service and repository exist, but there is no exposed REST endpoint in that controller yet

Why it exists:
- the system needs a way to find parking spaces by location
- the parking entity is the base object that reservations point to

### Reservation
Files in `src/main/java/com/smart/parking/reservation`

This module handles bookings.

Important files:
- `ReservationController.java`
- `ReservationService.java`
- `Reservation.java`
- `ReservationRepository.java`
- `BookingRequest.java`

What it does:
- books a parking space for a time period
- reduces available slots on the selected parking space
- creates a QR code token for the reservation
- stores reservation amount, time range, and status fields

Why it exists:
- this is the core business flow of the app
- everything else leads toward a reservation

### Payment
Files in `src/main/java/com/smart/parking/payment`

This module handles payment initiation and webhook processing.

Important files:
- `PaymentController.java`
- `PaymentService.java`
- `Payment.java`
- `PaymentRepository.java`

What it does:
- creates a payment link for a reservation
- receives webhook callbacks from Flutterwave
- marks a reservation as paid when payment succeeds
- stores payment records in the database

Why it exists:
- a reservation is not complete until payment is handled
- payment state must be stored separately from reservation state

### QR
Files in `src/main/java/com/smart/parking/qr`

This module generates and verifies QR codes.

Important files:
- `QrController.java`
- `QrService.java`
- `VerifyRequest.java`

What it does:
- generates a QR image for a reservation
- verifies a QR code at entry

Why it exists:
- the host or gatekeeper needs a quick way to confirm a booking
- QR codes make check-in faster than manually searching records

### Events
Files in `src/main/java/com/smart/parking/event`

Important files:
- `Event.java`
- `EventController.java`
- `EventService.java`
- `EventRepository.java`

What it does:
- models parking events
- can be linked to parking spaces
- supports event-driven pricing or activation logic

Important note:
- `EventController.java` is currently empty
- the entity and service exist, but the public REST side is not finished yet

### Admin
Files in `src/main/java/com/smart/parking/admin`

Important files:
- `AdminController.java`
- `AdminService.java`

What it does:
- represents administrator-level operations

Important note:
- `AdminController.java` is currently empty
- this part looks like a placeholder for future admin features

### Notifications and USSD
Files in `src/main/java/com/smart/parking/notification`

Important files:
- `UssdController.java`
- `NotificationService.java`
- related SMS and USSD helper classes

What it does:
- handles Africa's Talking USSD callbacks
- provides a simple menu-driven booking path for phones that do not use the web app

Why it exists:
- some users may not use smartphones
- USSD makes the system accessible through a basic phone dial flow

### Common and Config
Files in `src/main/java/com/smart/parking/common` and `src/main/java/com/smart/parking/config`

Important files:
- `ApiResponse.java`
- `GlobalExceptionHandler.java`
- `ResourceNotFoundException.java`
- `SecurityConfig.java`
- `SwaggerConfig.java`
- `AppConfig.java`
- `WebSocketConfig.java`
- `FirebaseConfig.java`

What it does:
- configures Spring Security
- configures Swagger/OpenAPI
- defines shared response shapes
- centralizes error handling
- sets application-level configuration

Why it exists:
- keeps the application consistent
- avoids repeating security and response code in every module

## 4. How The Main Objects Relate

Think of the data model like this:

- `User` is the person using the system
- `ParkingSpace` is the parking location
- `Reservation` connects a user to a parking space for a time range
- `Payment` records the money paid for a reservation
- `Event` can modify or organize parking behavior around special events
- `QR` is used to prove that a reservation is real at entry

The most important relationship is:

`User -> Reservation -> ParkingSpace -> Payment / QR`

That means:
- a user creates a reservation
- the reservation points to a parking space
- payment is created from the reservation
- QR verification later checks that reservation

## 5. How To Run It

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- a valid `.env` file or exported environment variables

### Runtime configuration
The application reads configuration from environment variables.

Important variables:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `PORT`

The YAML file defaults to PostgreSQL or H2 depending on environment variables, and the local setup in this workspace typically overrides the port and database values.

### Start the app
If using Maven directly:
```bash
mvn spring-boot:run
```

If using the local environment variables first:
```bash
export DB_URL='jdbc:postgresql://localhost:5432/smartparking'
export DB_USERNAME='postgres'
export DB_PASSWORD='Hello12@12'
mvn spring-boot:run
```

### Open Swagger
After startup, open Swagger UI in the browser and use it to explore the API.

Depending on the runtime port, the base URL will usually be one of:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8081/swagger-ui.html`

The root path also redirects to Swagger through `HealthController`.

### Quick health check
- `GET /ping` returns `pong`
- `/` redirects to Swagger

## 6. How To Use The API In Order

### Step 1: Register
Use `POST /api/auth/register`

Example body:
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+250712345678",
  "password": "SecurePass@123",
  "role": "DRIVER"
}
```

What happens:
- a user record is created
- a JWT token is returned
- the role is usually `DRIVER` unless you choose another role

### Step 2: Authorize
Copy the token from the response and click the Swagger Authorize button.

Use this format:
```text
Bearer YOUR_TOKEN_HERE
```

This matters because protected endpoints reject requests without the JWT header.

### Step 3: Create a reservation
Use `POST /reservations`

Example body:
```json
{
  "parkingSpaceId": 1,
  "slotCount": 1,
  "startTime": "2026-05-13T13:00:00",
  "endTime": "2026-05-13T15:00:00"
}
```

What happens:
- the user is identified from the token
- the selected parking space is loaded
- available slots are reduced
- total amount is calculated
- a reservation row is saved
- the reservation ID is returned

### Step 4: Start payment
Use `POST /payments/initiate/{reservationId}`

What happens:
- the backend creates a Flutterwave payment link
- the payment URL is returned
- the reservation can later be marked as paid by webhook callback

### Step 5: Generate QR
Use `GET /api/qr/generate?reservationId=1&userId=2`

What happens:
- a QR PNG is returned
- the code is intended for check-in or verification

### Step 6: Verify QR
Use `POST /api/qr/verify`

This is for HOST or ADMIN users.

What happens:
- the QR content is checked against the reservation
- a valid QR returns success
- an invalid or expired QR returns a bad request

### Step 7: USSD flow
Use the Africa's Talking callback on `POST /api/ussd`

What happens:
- the app returns menu text instead of JSON
- a user can browse nearby parking without using Swagger or a web app

## 7. What Endpoints Actually Exist Right Now

These are the endpoints implemented in the code you can see:

Public or mostly public:
- `GET /`
- `GET /ping`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/ussd`
- `POST /payments/webhook`

Authenticated:
- `POST /reservations`
- `POST /payments/initiate/{reservationId}`
- `GET /api/qr/generate`
- `POST /api/qr/verify`

Important note:
- some controller classes exist but are empty
- that means the codebase contains planned features that are not yet exposed as REST routes

## 8. Why The App Is Built This Way

### Controllers are thin
Controllers should only receive requests and return responses.

Why:
- easier to test
- easier to maintain
- less duplicated code

### Services hold business rules
The reservation rules, payment logic, and QR handling live in services.

Why:
- the real behavior belongs in one place
- controllers stay simple
- business logic is easier to reuse

### Repositories isolate database access
Repositories are the Spring Data JPA layer.

Why:
- you do not write raw SQL everywhere
- entities map directly to tables
- the code stays readable

### JWT is used for security
The app uses bearer tokens rather than sessions.

Why:
- suitable for APIs
- stateless and scalable
- works well with mobile clients and Swagger

### Flyway manages schema changes
Database schema changes live in migration files.

Why:
- the schema is versioned
- startup is repeatable
- existing databases can be upgraded safely

## 9. How To Read The Code

A good order for learning the codebase is:

1. Start with `SmartParkingApplication.java`
2. Read `application.yaml`
3. Read `SecurityConfig.java` and `SwaggerConfig.java`
4. Read `AuthController.java` and `AuthService.java`
5. Read `User.java` and `RegisterRequest.java`
6. Read `ParkingSpace.java` and `ParkingSpaceRepository.java`
7. Read `ReservationController.java` and `ReservationService.java`
8. Read `Reservation.java`
9. Read `PaymentController.java` and `PaymentService.java`
10. Read `QrController.java` and `QrService.java`
11. Read `UssdController.java`
12. Read the Flyway migration files in `src/main/resources/db/migration`

That order works because it follows the flow of a real request from entry point to persistence.

## 10. Common First-Time Mistakes

- Using the wrong phone format during registration
- Logging in with a different email or password than the ones used at registration
- Forgetting to add the Bearer token in Swagger
- Using `slotCount = 0`
- Trying to book before selecting a real parking space ID
- Expecting controllers to exist for classes that are still empty placeholders
- Confusing the default YAML port with the local overridden port

## 11. What Is Still Missing Or In Progress

Some parts of the codebase are clearly placeholders or incomplete:
- `ParkingController.java` is empty
- `EventController.java` is empty
- `AdminController.java` is empty
- there is no public endpoint shown here for listing parking spaces

So if you cannot find a route in Swagger, it may simply not be implemented yet.

## 12. Practical Story Of A User Journey

A typical successful journey looks like this:

1. Start the backend
2. Open Swagger
3. Register a user
4. Copy the JWT token
5. Authorize Swagger with the token
6. Create a reservation
7. Save the reservation ID
8. Initiate payment for that reservation
9. Generate or verify QR when needed

That is the main flow this backend is designed to support.

## 13. Short Version

If you only remember one thing, remember this:
- auth creates the identity
- reservation creates the booking
- payment marks it as paid
- QR proves it at the gate
- USSD is the alternate phone-based flow

## 14. Files To Open First

- `src/main/java/com/smart/parking/auth/AuthController.java`
- `src/main/java/com/smart/parking/auth/AuthService.java`
- `src/main/java/com/smart/parking/reservation/ReservationController.java`
- `src/main/java/com/smart/parking/reservation/ReservationService.java`
- `src/main/java/com/smart/parking/payment/PaymentController.java`
- `src/main/java/com/smart/parking/qr/QrController.java`
- `src/main/java/com/smart/parking/notification/UssdController.java`
- `src/main/resources/application.yaml`
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V3__align_schema_with_entities.sql`

## 15. Final Mental Model

If you are explaining the app to someone else, say this:

"Smart Parking is a Spring Boot backend that lets drivers register, log in, find parking, reserve a slot, pay for it, and prove the booking with QR or USSD. The backend is organized into controllers, services, repositories, entities, and migrations, with JWT security protecting most actions."
