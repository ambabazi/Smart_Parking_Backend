package com.smart.parking.security;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {
    void store(String refreshToken, String email, Duration ttl);

    Optional<String> resolveEmail(String refreshToken);

    void revoke(String refreshToken);
}
