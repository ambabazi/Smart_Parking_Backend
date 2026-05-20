package com.smart.parking.auth;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.security.JwtService;
import com.smart.parking.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration:900000}")
    private long accessTokenExpirationMs;

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

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful", buildAuthResponse(user)));
    }

    public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getEmail(), req.getPassword()));
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            return ResponseEntity.ok(ApiResponse.success("Login successful", buildAuthResponse(user)));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
        }
    }

    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(RefreshTokenRequest request) {
        try {
            String email = refreshTokenService.resolveEmail(request.getRefreshToken())
                    .orElseThrow(() -> new BadCredentialsException("Refresh token expired"));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            String accessToken = jwtService.generateToken(user);
            RefreshTokenService.RefreshTokenPair rotated = refreshTokenService.rotate(request.getRefreshToken(), accessToken);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed", buildAuthResponse(user, rotated.accessToken(), rotated.refreshToken(), rotated.refreshTokenExpiresInMs())));
        } catch (BadCredentialsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Refresh token expired or invalid"));
        }
    }

    public ResponseEntity<ApiResponse<Void>> logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserProfileDTO profile = UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", profile));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.issueTokens(user, accessToken);
        return buildAuthResponse(user, pair.accessToken(), pair.refreshToken(), pair.refreshTokenExpiresInMs());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken, long refreshTokenExpiresInMs) {
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessTokenExpiresInMs(accessTokenExpirationMs)
                .refreshTokenExpiresInMs(refreshTokenExpiresInMs)
                .build();
    }
}