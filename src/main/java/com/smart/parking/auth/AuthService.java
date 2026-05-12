package com.smart.parking.auth;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public ResponseEntity<ApiResponse<AuthResponse>> register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email already registered"));
        }
        if (userRepository.existsByPhone(req.getPhone())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Phone number already registered"));
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : Role.DRIVER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful",
                AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build()));
    }

    public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
        }

        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(ApiResponse.success("Login successful",
            AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build()));
    }
}