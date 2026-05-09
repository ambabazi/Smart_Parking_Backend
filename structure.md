
KIGALI SMART PARKING
Backend Engineer 1 — Complete Personal Guide

"Kigali doesn't necessarily need more parking buildings. It needs better utilization of the parking spaces that already exist."

Auth & JWT
QR Verification
Swagger UI
Railway Deploy

Agnes Uwase — Backend Engineer 1 (BE1)  |  48-Hour Build  |  IgireRwanda

WHAT IS IN THIS GUIDE
1. PART 1  —  Prerequisites & Tools Installation
2. PART 2  —  Creating & Initialising the Monorepo
3. PART 3  —  Spring Boot Project Setup
4. PART 4  —  Every File You Need — Directory Map & Purpose
5. PART 5  —  Building Auth: Registration, Login, JWT
6. PART 6  —  Building QR Code Generation & Verification
7. PART 7  —  Swagger UI Setup & How to Test Every Endpoint
8. PART 8  —  GitHub Safety Rules — What to Check Before Every Push
9. PART 9  —  Deployment to Railway — Step by Step
10. PART 10 —  Post-Deploy Smoke Tests on the Live URL

PART 1 — Prerequisites & Tools Installation
Install everything on this list before touching any code. Skipping steps here causes mysterious errors later.

1.1  Required Software
TOOL
VERSION
WHERE TO GET IT
Java Development Kit (JDK)
21 (LTS)
adoptium.net → Temurin 21
Apache Maven
3.9+
Bundled in Spring Boot — use ./mvnw wrapper
IntelliJ IDEA Community
Latest
jetbrains.com/idea (free Community edition)
Git
Latest
git-scm.com
Docker Desktop
Latest
docker.com/products/docker-desktop — for local DB
Postman
Latest
postman.com — for testing before Swagger is set up
pgAdmin 4 (optional)
Latest
pgadmin.org — visual PostgreSQL browser

1.2  Verify Your Java Installation
Open a terminal and run:

java -version
# You must see: openjdk version "21..."

javac -version
# You must see: javac 21...

⚠  If you see version 17 or lower
You have an older JDK installed. Either uninstall it or set JAVA_HOME to point to JDK 21.
On Windows: Search 'Environment Variables' → set JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-21...
On Mac/Linux: export JAVA_HOME=$(/usr/libexec/java_home -v 21)

1.3  Start PostgreSQL with Docker
This gives you a clean database without installing PostgreSQL natively. Run this once and leave it running.

docker run --name parking_db \
-e POSTGRES_DB=parking_db \
-e POSTGRES_USER=parking \
-e POSTGRES_PASSWORD=parking123 \
-p 5432:5432 \
-d postgres:15-alpine

# Verify it started:
docker ps
# You should see 'parking_db' in the list with status 'Up'

To stop it later:  docker stop parking_db
To start it again: docker start parking_db

PART 2 — Creating & Initialising the Monorepo
You are the repo owner and reviewer. You run these commands ONCE on Day 1 at 8:00 AM before the rest of the team does anything.

2.1  Create the Repo on GitHub First
1
Log in to GitHub
Go to github.com and sign in to your account.
2
Create a new repository
Click the '+' icon top-right → 'New repository'.
3
Fill in the details
Repository name: kigali-parking   |   Visibility: Public   |   Do NOT tick 'Add README' — you will push your own.
4
Click 'Create repository'
Copy the HTTPS URL shown, e.g. https://github.com/YOUR_USERNAME/kigali-parking.git

2.2  Initialise the Monorepo Locally

# Run from your home folder or wherever you keep projects
mkdir kigali-parking
cd kigali-parking

# Create the folder structure
mkdir -p backend frontend docs .github/workflows

# Create placeholder files so git tracks the folders
touch docs/ENDPOINTS.md
touch .github/pull_request_template.md
touch .gitignore
touch README.md
touch docker-compose.yml

git init
git add .
git commit -m "chore: monorepo scaffold"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/kigali-parking.git
git push -u origin main


2.3  Create the develop Branch & Protect main

git checkout -b develop
git push -u origin develop

Now go to GitHub to protect the main branch so nobody (including you by accident) can push directly to it:
1
Go to repository Settings
Settings tab → Branches (left sidebar).
2
Add a branch protection rule
Click 'Add branch ruleset' → Name it 'Protect main'.
3
Set target to main
Under 'Target branches' → Add target → Branch name → type: main.
4
Enable these rules
Tick: Require a pull request before merging  |  Tick: Require approvals (set to 1 = you)  |  Tick: Block force pushes.
5
Save
Click 'Create' at the bottom. main is now protected.

2.4  Create Your Feature Branch

# Always work on this branch, never on develop or main
git checkout develop
git checkout -b feature/be1-auth
git push -u origin feature/be1-auth

You are now ready
Share the repo URL with your teammates in the group chat.
They clone it, then each creates their own branch (be2-events, fe1-map, fe2-booking).
You review and merge all PRs from now on.

PART 3 — Spring Boot Project Setup
You generate the Spring Boot project and commit it into the backend/ folder of the monorepo.

3.1  Generate the Project with Spring Initializr
1
Open start.spring.io in your browser
This is the official project generator.
2
Set these values
Project: Maven  |  Language: Java  |  Spring Boot: 3.2.x  |  Group: com.kigaliparking  |  Artifact: backend  |  Packaging: Jar  |  Java: 21
3
Add these dependencies
Search and add each one: Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Flyway Migration, Spring Boot DevTools, Lombok, Validation, WebSocket.
4
Click Generate
Downloads backend.zip to your Downloads folder.
5
Move into the monorepo
Unzip backend.zip, copy its contents into your kigali-parking/backend/ folder.

3.2  Add Remaining Dependencies to pom.xml
Open backend/pom.xml and add these inside the <dependencies> section — Spring Initializr does not have them:

<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>

<!-- QR Code -->
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>core</artifactId>
  <version>3.5.2</version>
</dependency>
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>javase</artifactId>
  <version>3.5.2</version>
</dependency>

<!-- Swagger UI / OpenAPI -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>


3.3  Configure application.properties
Create the file: backend/src/main/resources/application.properties

# ── Server ─────────────────────────────────────────────────────────────
server.port=8080

# ── Database ────────────────────────────────────────────────────────────
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/parking_db}
spring.datasource.username=${DB_USERNAME:parking}
spring.datasource.password=${DB_PASSWORD:parking123}
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ─────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Flyway ──────────────────────────────────────────────────────────────
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# ── JWT ─────────────────────────────────────────────────────────────────
jwt.secret=${JWT_SECRET:kigali_parking_super_secret_key_change_in_prod_64_chars}
jwt.expiration=${JWT_EXPIRATION:86400000}

# ── Flutterwave ─────────────────────────────────────────────────────────
flutterwave.secret.key=${FLW_SECRET_KEY:}
flutterwave.public.key=${FLW_PUBLIC_KEY:}
flutterwave.base.url=https://api.flutterwave.com/v3

# ── Frontend URL (for CORS + payment redirect) ──────────────────────────
app.frontend.url=${FRONTEND_URL:http://localhost:3000}

# ── Springdoc / Swagger ─────────────────────────────────────────────────
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true

# ── Async / Scheduling ──────────────────────────────────────────────────
spring.task.scheduling.pool.size=2


3.4  Verify the Project Starts

cd backend
./mvnw spring-boot:run

# You should see something like:
# Started BackendApplication in 4.2 seconds

# If you see 'Connection refused' for the database,
# make sure Docker is running and the parking_db container is up.


PART 4 — Every File You Need: Directory Map & Purpose
This is the complete map of every file you (BE1) will create. Each file has one job. Never put logic for two different concerns in the same file.

4.1  Full Directory Tree

backend/
├── src/
│   ├── main/
│   │   ├── java/com/kigaliparking/
│   │   │   ├── BackendApplication.java       ← Entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java        ← Spring Security rules
│   │   │   │   ├── CorsConfig.java            ← Allow React to call us
│   │   │   │   ├── WebSocketConfig.java       ← Real-time slots (BE2 creates)
│   │   │   │   └── SwaggerConfig.java         ← Swagger UI customisation
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java        ← POST /auth/register, /login
│   │   │   │   └── QRController.java          ← POST /verify-qr, GET QR image
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java       ← Input for register
│   │   │   │   ├── LoginRequest.java          ← Input for login
│   │   │   │   ├── AuthResponse.java          ← Output: token + user
│   │   │   │   ├── UserDto.java               ← Safe user object (no password)
│   │   │   │   └── QRVerifyRequest.java       ← Input: token string
│   │   │   ├── entity/
│   │   │   │   ├── User.java                  ← Users table
│   │   │   │   └── enums/
│   │   │   │       └── Role.java              ← DRIVER, HOST, ADMIN
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java        ← DB queries for users
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java               ← Create & validate JWT tokens
│   │   │   │   ├── JwtAuthFilter.java         ← Intercept every request
│   │   │   │   └── CustomUserDetailsService.java ← Load user for Spring Security
│   │   │   └── service/
│   │   │       ├── AuthService.java           ← Register & login logic
│   │   │       └── QRService.java             ← Generate & verify QR codes
│   │   └── resources/
│   │       ├── application.properties         ← All config & env vars
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_users.sql           ← Users table
│   │               ├── V2__create_parking_spaces.sql  ← BE2 creates
│   │               ├── V3__create_reservations.sql    ← BE2 creates
│   │               ├── V4__create_events.sql          ← BE2 creates
│   │               ├── V5__create_payments.sql        ← BE2 creates
│   │               └── V6__seed_kigali_data.sql       ← BE2 creates
│   └── test/
│       └── java/com/kigaliparking/
│           └── controller/
│               └── AuthControllerTest.java    ← Basic smoke tests
├── Dockerfile                                 ← For Railway deploy
├── pom.xml                                    ← All dependencies
└── .mvn/wrapper/maven-wrapper.properties      ← Auto-generated


4.2  File-by-File Purpose Table
BE1 files — every file you personally create:
FILE
TYPE
OWNER
PURPOSE
BackendApplication.java
Entry point
BE1
The main() method that starts Spring Boot. One line. Touch it once, never again.
config/SecurityConfig.java
Config
BE1
Defines which endpoints are public (/auth/**) and which require a JWT. Also registers the JWT filter.
config/CorsConfig.java
Config
BE1
Allows the React frontend (localhost:3000 / Vercel URL) to call the backend. Without this, browsers block all requests.
config/SwaggerConfig.java
Config
BE1
Adds the JWT Bearer token input to Swagger UI so you can test protected endpoints from the browser.
controller/AuthController.java
Controller
BE1
Two endpoints: POST /auth/register and POST /auth/login. Receives HTTP requests, calls AuthService, returns JSON.
controller/QRController.java
Controller
BE1
POST /verify-qr (for attendants), GET /reservations/{id}/qr-image (returns PNG). Calls QRService.
dto/RegisterRequest.java
DTO
BE1
The JSON shape the client sends to /auth/register. Has @NotBlank/@Email validations.
dto/LoginRequest.java
DTO
BE1
The JSON shape the client sends to /auth/login. Email + password.
dto/AuthResponse.java
DTO
BE1
The JSON shape we return after login/register. Contains the JWT token and a UserDto.
dto/UserDto.java
DTO
BE1
A safe view of the User entity — never includes the password hash.
dto/QRVerifyRequest.java
DTO
BE1
The JSON the attendant's scanner sends — just { token: "uuid-string" }.
entity/User.java
Entity
BE1
JPA entity that maps to the users table. Fields: id, name, email, password, role, plateNumber, createdAt.
entity/enums/Role.java
Enum
BE1
Java enum: DRIVER, HOST, ADMIN. Used by User entity and security rules.
repository/UserRepository.java
Repository
BE1
Spring Data JPA interface. Provides findByEmail(), existsByEmail() — no SQL needed.
security/JwtUtil.java
Security
BE1
generateToken(user), validateToken(token), extractEmail(token). Uses the JJWT library.
security/JwtAuthFilter.java
Security
BE1
Runs before every request. Reads the Authorization header, validates the JWT, sets the security context.
security/CustomUserDetailsService.java
Security
BE1
Loads a User from the DB by email for Spring Security's authentication flow.
service/AuthService.java
Service
BE1
register(): hash password, save user, generate token. login(): find user, check password, generate token.
service/QRService.java
Service
BE1
generateQRImage(token): uses ZXing to create a PNG byte array. verifyToken(token): queries reservation by qrCode field.
V1__create_users.sql
Migration
BE1
Creates the users table. Flyway runs this automatically on startup. Never edit after it has run.
AuthControllerTest.java
Test
BE1
Two basic tests: register endpoint returns 200 with token, login with wrong password returns 401.
Dockerfile
DevOps
BE1
Multi-stage Docker build. Used by Railway to containerise the app for deployment.

PART 5 — Building Auth: Registration, Login, JWT
Build in this exact order. Each piece depends on the one before it.

5.1  Database Migration — V1__create_users.sql
Create this file at: backend/src/main/resources/db/migration/V1__create_users.sql
Flyway runs this automatically the first time the app starts. It creates the users table.

CREATE TYPE user_role AS ENUM ('DRIVER', 'HOST', 'ADMIN');

CREATE TABLE users (
id           BIGSERIAL PRIMARY KEY,
name         VARCHAR(100)  NOT NULL,
email        VARCHAR(150)  NOT NULL UNIQUE,
password     VARCHAR(255)  NOT NULL,
role         user_role     NOT NULL DEFAULT 'DRIVER',
plate_number VARCHAR(20),
created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);


5.2  Role Enum
Create: entity/enums/Role.java

package com.kigaliparking.entity.enums;

public enum Role {
DRIVER,
HOST,
ADMIN
}


5.3  User Entity
Create: entity/User.java

package com.kigaliparking.entity;

import com.kigaliparking.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 100)
    private String name;
 
    @Column(nullable = false, unique = true, length = 150)
    private String email;
 
    @Column(nullable = false)
    private String password;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.DRIVER;
 
    @Column(name = "plate_number", length = 20)
    private String plateNumber;
 
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    // Spring Security interface methods
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}


5.4  JWT Utility
Create: security/JwtUtil.java

package com.kigaliparking.security;

import com.kigaliparking.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
 
    @Value("${jwt.expiration}")
    private long expiration;
 
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
 
    public String generateToken(User user) {
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey())
            .compact();
    }
 
    public String extractEmail(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }
 
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}


5.5  Security Config & CORS
Create: config/SecurityConfig.java

package com.kigaliparking.config;

import com.kigaliparking.security.JwtAuthFilter;
import com.kigaliparking.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
 
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
 
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token needed
                .requestMatchers(
                    "/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/payments/webhook"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
 
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


Create: config/CorsConfig.java

package com.kigaliparking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;
 
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            frontendUrl,
            "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
 
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


PART 6 — Building QR Code Generation & Verification
The QR system is entirely yours (BE1). It generates a PNG QR code image for each reservation, and lets attendants verify it.

6.1  QR Service
Create: service/QRService.java

package com.kigaliparking.service;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class QRService {

    /**
     * Generates a PNG QR code image for the given token.
     * The token is stored in Reservation.qrCode.
     * Returns the raw PNG bytes — the controller turns these into an HTTP response.
     */
    public byte[] generateQRImage(String token) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix  = writer.encode(token, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }
}


6.2  QR Controller
Create: controller/QRController.java

package com.kigaliparking.controller;

import com.kigaliparking.service.QRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "QR Verification", description = "Generate and verify parking QR codes")
public class QRController {

    private final QRService qrService;
    // BE2 will inject ReservationRepository — for now we wire it when be2-events merges
 
    public QRController(QRService qrService) {
        this.qrService = qrService;
    }
 
    @PostMapping("/verify-qr")
    @Operation(summary = "Verify a QR token scanned by a parking attendant")
    public ResponseEntity<?> verifyQR(@RequestBody QRVerifyRequest req) {
        // TODO: wire up ReservationRepository after BE2 merges
        // For now: return a mock success response for testing
        return ResponseEntity.ok(new VerifyResponse(true, "Entry approved (mock)"));
    }
 
    @GetMapping("/reservations/{reservationId}/qr-image")
    @Operation(summary = "Get QR code PNG image for a reservation")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<byte[]> getQRImage(@PathVariable Long reservationId) throws Exception {
        // TODO: look up actual qrCode token from DB after BE2 merges
        String mockToken = "MOCK-TOKEN-" + reservationId;
        byte[] image = qrService.generateQRImage(mockToken);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(image);
    }
 
    record QRVerifyRequest(String token) {}
    record VerifyResponse(boolean valid, String message) {}
}


PART 7 — Swagger UI Setup & How to Test Every Endpoint
Swagger UI gives you an interactive browser interface to call every endpoint without writing curl commands. It is also what your team uses to test the live deployed API.

7.1  SwaggerConfig.java
Create: config/SwaggerConfig.java

package com.kigaliparking.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Kigali Smart Parking API")
                .description("REST API for the Kigali Smart Parking System. " +
                    "To test protected endpoints: click 'Authorize' and paste your JWT token.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("IgireRwanda Team")
                    .email("team@igirerwanda.rw")))
            // This adds the 'Authorize' button to Swagger UI
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Paste your JWT token here (without the 'Bearer ' prefix)")
                ));
    }
}


7.2  Add Swagger Annotations to Your Controllers
Add these imports and annotations to AuthController.java — they appear as descriptions in the Swagger UI:

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register and login — no token required")
public class AuthController {

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a DRIVER, HOST, or ADMIN account. Returns a JWT token.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Email already in use or validation failed")
        }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }
 
    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}


7.3  Access Swagger UI

# Start the backend
./mvnw spring-boot:run

# Open in browser:
http://localhost:8080/swagger-ui.html

# You will see the full API documentation with Try It Out buttons.


7.4  Testing Flow in Swagger UI — Step by Step
Follow this sequence every time you add a new endpoint:
1
Open Swagger UI
Navigate to http://localhost:8080/swagger-ui.html in your browser.
2
Test POST /auth/register
Click on POST /auth/register → 'Try it out' → fill in the JSON body → Execute. You should see status 200 and a token in the response.
3
Copy the JWT token
In the response body, copy just the token string value (the long string starting with 'eyJ...').
4
Authorize in Swagger
Click the green 'Authorize' button at the top right of the page → paste the token into the Value box → click Authorize → Close.
5
Test protected endpoints
Now any endpoint with the padlock icon will automatically include your JWT. Test GET /reservations/my — it should return your reservations, not a 401.
6
Test error cases
Register again with the same email — you should get 400 with a message. Try a protected endpoint without authorizing — you should get 401.

7.5  Testing the QR Endpoint in Swagger
1
Authorize first
Complete steps 1-4 above to get a token into Swagger.
2
Call GET /reservations/{reservationId}/qr-image
Enter any number (e.g. 1) as reservationId → Execute.
3
Download the response
Swagger shows a 'Download' button for binary responses. Click it — a PNG file is saved to your downloads.
4
Open the PNG
Open it — you should see a real scannable QR code. Scan it with your phone camera to confirm it decodes to the token text.

Swagger URL on Railway (after deploy)
Once deployed, your Swagger UI is live at:
https://YOUR-APP.railway.app/swagger-ui.html
Share this URL with your frontend team — they can read all endpoint shapes directly from it.

PART 8 — GitHub Safety Rules — What to Check Before Every Push
GitHub has automated scanners that will flag your repo and send you warnings if you accidentally commit secrets, credentials, or keys. Follow these rules every single time before pushing.

GitHub Secret Scanning — what gets flagged
AWS keys, Google API keys, Stripe keys, JWT secrets in code.
Any string matching patterns like 'sk_live_', 'AKIA...', 'eyJhbGciOi...' hardcoded in .java or .properties files.
Flutterwave secret keys (FLWSECK_...) committed in any file.
Database passwords in application.properties that is tracked by git.

8.1  The .gitignore File — Check It Before First Push
Make sure your root .gitignore (already set up in Part 2) contains these entries. Verify with: cat .gitignore

# These files must NEVER reach GitHub
backend/.env
backend/src/main/resources/application-local.properties
backend/target/
*.class
*.jar


8.2  The Pre-Push Checklist — Run This Every Time
Before running git push, run through each of these:

CHECK
STATUS
1
Run: grep -r "FLWSECK" backend/src — must return NO results
⬜ TODO
2
Run: grep -r "jwt.secret" backend/src — only application.properties should appear, and its value must use ${JWT_SECRET:...} not a hardcoded string
⬜ TODO
3
Run: grep -r "password" backend/src/main/resources — must show ${DB_PASSWORD:...} not a real password
⬜ TODO
4
Run: git status — verify no .env file is listed as staged
⬜ TODO
5
Run: git diff --cached -- backend/src — visually scan for any string that looks like an API key (long random strings)
⬜ TODO
6
application.properties must use ${ENV_VAR:default} pattern for all secrets, never raw values
⬜ TODO
7
No System.out.println() calls that might log tokens or passwords
⬜ TODO
8
The app compiles: ./mvnw package -DskipTests — must end with BUILD SUCCESS
⬜ TODO

8.3  If You Accidentally Commit a Secret
Act immediately. A secret is exposed the moment it reaches GitHub, even if you delete it in the next commit — the history still has it.
1
Revoke the key immediately
Go to Flutterwave / Google / wherever the key is from and regenerate it. The old one is now compromised.
2
Remove from git history
Run: git filter-branch --force --index-filter "git rm --cached --ignore-unmatch backend/src/main/resources/application.properties" --prune-empty --tag-name-filter cat -- --all
3
Force push
git push origin --force --all    (this overwrites the history on GitHub)
4
Add the file to .gitignore
So it never happens again with that file.

8.4  Correct Commit Workflow

# 1. Check what you are about to stage
git status

# 2. Stage only your source code (never .env or target/)
git add backend/src/
git add backend/pom.xml
git add backend/Dockerfile
# do NOT run: git add .  (this might include .env)

# 3. Run the secret scan
grep -r "FLWSECK\|AIza\|sk_live\|password=\"" backend/src/
# Must return: nothing

# 4. Commit with a descriptive message
git commit -m "feat: BE1-03 add JWT authentication filter"

# 5. Push to YOUR branch only
git push origin feature/be1-auth

# 6. Open a Pull Request on GitHub: feature/be1-auth → develop
# Never push directly to develop or main.


8.5  PR Description Template
When opening a Pull Request, fill this in so your own review is easy:

## What this does
Implements JWT authentication — register and login endpoints.
Token is returned on success and must be sent as Bearer in subsequent requests.

## Task
BE1-03

## Tested in Swagger
- [x] POST /auth/register → 200 + token
- [x] POST /auth/login → 200 + token
- [x] POST /auth/login with wrong password → 401
- [x] GET /reservations/my without token → 401
- [x] GET /reservations/my with valid token → 200 (empty list)

## Secret scan
- [x] grep -r FLWSECK backend/src → no results
- [x] application.properties uses ${} env vars only


PART 9 — Deployment to Railway — Step by Step
Railway is the free hosting platform for the backend. It reads your Dockerfile, pulls from GitHub, and runs the app automatically. PostgreSQL is included.

9.1  Write the Dockerfile
Create: backend/Dockerfile

# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom first (for layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Stage 2: Run with slim JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]


9.2  Create a Railway Account
1
Go to railway.app
Click 'Start a New Project' → Sign up with GitHub (this links your account to your repos automatically).
2
Verify your account
Check your email for the verification link from Railway and click it.
3
Free tier
Railway gives 500 hours/month and 1GB database for free — enough for the hackathon demo and beyond.

9.3  Create the PostgreSQL Database on Railway
1
New Project
Dashboard → 'New Project' → 'Provision PostgreSQL'.
2
Wait for it to provision
Takes about 30 seconds. You will see a green 'Active' status.
3
Copy the connection string
Click the PostgreSQL service → Variables tab → copy the value of DATABASE_URL. It looks like: postgresql://postgres:password@roundhouse.proxy.rlwy.net:PORT/railway
4
Keep this tab open
You will paste this value into the backend service variables in Step 9.5.

9.4  Deploy the Backend Service
1
Add a new service to the project
In your Railway project → '+' → 'GitHub Repo'.
2
Select your repo
Choose kigali-parking from the list.
3
Set the root directory
In the service settings → Source → Root Directory → type: backend
4
Railway detects the Dockerfile
It will show 'Dockerfile detected' and start building automatically.
5
Wait for the first build
First build takes 3-5 minutes (it downloads all Maven dependencies). Watch the build logs.

9.5  Set Environment Variables on Railway
This is the critical step. Your app will crash on startup without these. Go to the backend service → Variables tab → add each one:
VARIABLE NAME
VALUE TO SET
DB_URL
Paste the DATABASE_URL from Railway PostgreSQL (starts with postgresql://)
DB_USERNAME
postgres (or whatever Railway shows in the PostgreSQL Variables tab)
DB_PASSWORD
The PGPASSWORD value from Railway PostgreSQL Variables tab
JWT_SECRET
Generate a random 64-character string — use: openssl rand -hex 32
JWT_EXPIRATION
86400000  (24 hours in milliseconds)
FLW_SECRET_KEY
Your Flutterwave TEST secret key from dashboard.flutterwave.com
FLW_PUBLIC_KEY
Your Flutterwave TEST public key
FRONTEND_URL
Your Vercel frontend URL (set after FE1 deploys) — e.g. https://kigali-parking.vercel.app

Important — DB_URL format for Spring Boot
Railway gives you: postgresql://user:password@host:port/database
Spring Boot needs: jdbc:postgresql://host:port/database  (no user:password in URL)

So if Railway gives you: postgresql://postgres:abc123@roundhouse.proxy.rlwy.net:5432/railway
Set DB_URL to:           jdbc:postgresql://roundhouse.proxy.rlwy.net:5432/railway
Set DB_USERNAME to:      postgres
Set DB_PASSWORD to:      abc123

9.6  Get Your Public URL
1
Go to the backend service Settings tab
Find the 'Networking' section → 'Generate Domain'.
2
Click Generate Domain
Railway gives you a public URL like: kigali-parking-production.up.railway.app
3
Share this URL with your team
FE1 and FE2 set this as REACT_APP_API_URL in their Vercel environment variables.
4
Your Swagger UI is now live
https://kigali-parking-production.up.railway.app/swagger-ui.html — share with the whole team.
9.7  Auto-Deploy on Push
From now on, every push to main on GitHub automatically triggers a new Railway build. The flow is:

Your code → feature/be1-auth
↓  (PR → Agnes reviews → merge)
develop branch
↓  (Agnes merges develop → main at end of day)
main branch
↓  (Railway webhook fires automatically)
New build starts on Railway
↓  (3-5 minutes)
Live deployment updated


PART 10 — Post-Deploy Smoke Tests on the Live URL
After every deployment, run through this test sequence in Swagger UI using your live Railway URL. These tests confirm the deployment succeeded and the database is connected.

10.1  Health Check — First Test

# Open in browser — if you see the Swagger UI, the app is running:
https://YOUR-APP.up.railway.app/swagger-ui.html

# If you see a 502 or blank page, check the Railway build logs:
# Dashboard → your service → Deployments → click the latest → View Logs


10.2  Full Smoke Test Sequence
Run all of these in Swagger UI using the live URL. Mark each one as it passes.
#
ENDPOINT
WHAT TO SEND
EXPECTED RESULT
✓
1
POST /auth/register
{ "name":"Test User", "email":"smoke@test.com", "password":"Test123!", "role":"DRIVER" }
200 — response contains token string
⬜
2
POST /auth/register (same email)
Same body as test 1
400 — 'Email already in use'
⬜
3
POST /auth/login
{ "email":"smoke@test.com", "password":"Test123!" }
200 — token in response
⬜
4
POST /auth/login (wrong password)
{ "email":"smoke@test.com", "password":"wrong" }
401 — Unauthorized
⬜
5
Authorize in Swagger
Paste token from test 3 into Authorize dialog
Padlock icon closes
⬜
6
GET /parking-spaces/nearby (BE2 endpoint)
?lat=-1.95&lng=30.10&radius=2000
200 — array of parking spaces (even if empty)
⬜
7
POST /verify-qr
{ "token": "test-token-123" }
200 — mock verification response
⬜
8
GET /reservations/1/qr-image
No body — click Download
200 — PNG file downloads and displays a QR code
⬜
9
GET /reservations/my (without token)
Remove Authorization first
401 — Unauthorized
⬜
10
GET /events/active (BE2 endpoint)
No body needed
200 — array of events (even if empty)
⬜

10.3  Interpreting Common Errors
ERROR
MOST LIKELY CAUSE
HOW TO FIX
502 Bad Gateway
App crashed on startup — DB not connected
Check Railway logs → look for 'Connection refused' or 'password authentication failed'
500 on register
Flyway migration failed — table doesn't exist
Logs will say 'relation users does not exist'. Check V1__create_users.sql syntax.
401 on register
Security config is blocking /auth/**
Check SecurityConfig — /auth/** must be in .permitAll() list
CORS error in browser
Frontend URL not in CorsConfig allowedOrigins
Add the exact Vercel URL to CorsConfig.java, redeploy
JWT invalid after deploy
JWT_SECRET env var not set on Railway
Go to Railway Variables tab, verify JWT_SECRET has a value

10.4  Keep Railway Alive During Demo
Railway's free tier puts apps to sleep after 30 minutes of inactivity. Add a cron job to ping it:

# Add an /actuator/health endpoint (Spring Boot Actuator):
# Add to pom.xml:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

# Add to application.properties:
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never

# Health endpoint (make this public in SecurityConfig):
# /actuator/health → returns { status: 'UP' }

# Use cron-job.org (free) to ping every 14 minutes:
# URL: https://YOUR-APP.up.railway.app/actuator/health
# Schedule: every 14 minutes
# This keeps the app awake during your demo day.


You are fully deployed when:
Swagger UI loads at your Railway URL.
POST /auth/register returns a JWT token.
The QR endpoint returns a downloadable PNG.
FE1 and FE2 have updated REACT_APP_API_URL to your Railway URL and their apps work.
cron-job.org is pinging /actuator/health every 14 minutes.

Agnes — you are BE1. Auth is the foundation everything else builds on.
Get the JWT working first. Everything else unlocks after that.
Get the JWT working first. Everything else unlocks after that.