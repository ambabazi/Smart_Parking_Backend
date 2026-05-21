package com.smart.parking.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void store(String refreshToken, String email, Duration ttl) {
        redisTemplate.opsForValue().set(redisKey(refreshToken), email, ttl);
    }

    @Override
    public Optional<String> resolveEmail(String refreshToken) {
        String email = redisTemplate.opsForValue().get(redisKey(refreshToken));
        return Optional.ofNullable(email);
    }

    @Override
    public void revoke(String refreshToken) {
        redisTemplate.delete(redisKey(refreshToken));
    }

    private String redisKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + RefreshTokenHasher.hash(refreshToken);
    }
}
