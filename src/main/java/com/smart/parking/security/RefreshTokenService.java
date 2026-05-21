package com.smart.parking.security;

import com.smart.parking.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenStore refreshTokenStore;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenPair issueTokens(User user, String accessToken) {
        String refreshToken = generateRefreshToken();
        store(refreshToken, user.getEmail());
        return new RefreshTokenPair(accessToken, refreshToken, refreshExpirationMs);
    }

    public Optional<String> resolveEmail(String refreshToken) {
        return refreshTokenStore.resolveEmail(refreshToken);
    }

    public boolean isValid(String refreshToken) {
        return resolveEmail(refreshToken).isPresent();
    }

    public RefreshTokenPair rotate(String refreshToken, String accessToken) {
        String email = resolveEmail(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token expired"));
        revoke(refreshToken);
        String nextRefreshToken = generateRefreshToken();
        store(nextRefreshToken, email);
        return new RefreshTokenPair(accessToken, nextRefreshToken, refreshExpirationMs);
    }

    public void revoke(String refreshToken) {
        refreshTokenStore.revoke(refreshToken);
    }

    private void store(String refreshToken, String email) {
        refreshTokenStore.store(refreshToken, email, Duration.ofMillis(refreshExpirationMs));
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return UUID.randomUUID() + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public record RefreshTokenPair(String accessToken, String refreshToken, long refreshTokenExpiresInMs) {}
}
