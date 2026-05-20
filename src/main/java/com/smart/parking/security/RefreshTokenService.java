package com.smart.parking.security;

import com.smart.parking.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenPair issueTokens(User user, String accessToken) {
        String refreshToken = generateRefreshToken();
        store(refreshToken, user.getEmail());
        return new RefreshTokenPair(accessToken, refreshToken, refreshExpirationMs);
    }

    public Optional<String> resolveEmail(String refreshToken) {
        String email = redisTemplate.opsForValue().get(redisKey(refreshToken));
        return Optional.ofNullable(email);
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
        redisTemplate.delete(redisKey(refreshToken));
    }

    private void store(String refreshToken, String email) {
        redisTemplate.opsForValue().set(redisKey(refreshToken), email, Duration.ofMillis(refreshExpirationMs));
    }

    private String redisKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + hash(refreshToken);
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return UUID.randomUUID() + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash refresh token", ex);
        }
    }

    public record RefreshTokenPair(String accessToken, String refreshToken, long refreshTokenExpiresInMs) {}
}
