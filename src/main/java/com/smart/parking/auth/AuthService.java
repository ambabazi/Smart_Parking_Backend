package com.smart.parking.auth;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.security.AppUserDetails;
import com.smart.parking.security.JwtService;
import com.smart.parking.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    @Autowired(required = false)
    private RefreshTokenService refreshTokenService;

    @Autowired(required = false)
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${jwt.expiration:900000}")
    private long accessTokenExpirationMs;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.auth.reset-token-ttl-minutes:30}")
    private long resetTokenTtlMinutes;

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
        if (refreshTokenService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Refresh token support is not configured"));
        }
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
        if (refreshTokenService != null) {
            refreshTokenService.revoke(request.getRefreshToken());
        }
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
                .notificationsEnabled(user.getNotificationsEnabled())
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .smsNotificationsEnabled(user.getSmsNotificationsEnabled())
                .preferredLanguage(user.getPreferredLanguage())
                .reminderMinutesBeforeEnd(user.getReminderMinutesBeforeEnd())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", profile));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().trim()).ifPresent(this::createResetToken);
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a password reset link has been prepared."));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken().trim());
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Reset token is invalid or expired"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }

    @Transactional
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String trimmedPhone = request.getPhone().trim();
        if (!user.getPhone().equals(trimmedPhone) && userRepository.existsByPhone(trimmedPhone)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Phone number already registered"));
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(trimmedPhone);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Profile updated", toProfileDTO(user)));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
    }

    @Transactional
    public ResponseEntity<ApiResponse<NotificationPreferencesDTO>> updateNotifications(String email, UpdatePreferencesRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getNotificationsEnabled() != null) {
            user.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getSmsNotificationsEnabled() != null) {
            user.setSmsNotificationsEnabled(request.getSmsNotificationsEnabled());
        }
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", toPreferencesDTO(user)));
    }

    @Transactional
    public ResponseEntity<ApiResponse<NotificationPreferencesDTO>> updatePreferences(String email, UpdatePreferencesRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getNotificationsEnabled() != null) {
            user.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getSmsNotificationsEnabled() != null) {
            user.setSmsNotificationsEnabled(request.getSmsNotificationsEnabled());
        }
        if (request.getPreferredLanguage() != null && !request.getPreferredLanguage().isBlank()) {
            user.setPreferredLanguage(request.getPreferredLanguage().trim());
        }
        if (request.getReminderMinutesBeforeEnd() != null) {
            user.setReminderMinutesBeforeEnd(request.getReminderMinutesBeforeEnd());
        }
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Preferences updated", toPreferencesDTO(user)));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        if (refreshTokenService == null) {
            return buildAuthResponse(user, accessToken, null, null);
        }
        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.issueTokens(user, accessToken);
        return buildAuthResponse(user, pair.accessToken(), pair.refreshToken(), pair.refreshTokenExpiresInMs());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken, Long refreshTokenExpiresInMs) {
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

    private UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .notificationsEnabled(user.getNotificationsEnabled())
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .smsNotificationsEnabled(user.getSmsNotificationsEnabled())
                .preferredLanguage(user.getPreferredLanguage())
                .reminderMinutesBeforeEnd(user.getReminderMinutesBeforeEnd())
                .build();
    }

    private NotificationPreferencesDTO toPreferencesDTO(User user) {
        return NotificationPreferencesDTO.builder()
                .notificationsEnabled(user.getNotificationsEnabled())
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .smsNotificationsEnabled(user.getSmsNotificationsEnabled())
                .preferredLanguage(user.getPreferredLanguage())
                .reminderMinutesBeforeEnd(user.getReminderMinutesBeforeEnd())
                .build();
    }

    private void createResetToken(User user) {
        if (passwordResetTokenRepository == null) {
            System.out.println("Password reset requested for " + user.getEmail() + " but reset-token persistence is not configured.");
            return;
        }

        String token = generateToken();
        String tokenHash = hashToken(token);
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(resetTokenTtlMinutes))
                .build());

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        System.out.println("Password reset link for " + user.getEmail() + ": " + resetUrl);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hashed) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash reset token", e);
        }
    }
}