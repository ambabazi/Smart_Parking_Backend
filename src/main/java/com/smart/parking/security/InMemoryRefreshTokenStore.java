package com.smart.parking.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    @Override
    public void store(String refreshToken, String email, Duration ttl) {
        tokens.put(hash(refreshToken), new Entry(email, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<String> resolveEmail(String refreshToken) {
        Entry entry = tokens.get(hash(refreshToken));
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            tokens.remove(hash(refreshToken));
            return Optional.empty();
        }
        return Optional.of(entry.email());
    }

    @Override
    public void revoke(String refreshToken) {
        tokens.remove(hash(refreshToken));
    }

    private String hash(String refreshToken) {
        return RefreshTokenHasher.hash(refreshToken);
    }

    private record Entry(String email, Instant expiresAt) {}
}
