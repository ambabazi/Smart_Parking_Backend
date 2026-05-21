package com.smart.parking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "parkingSpaces",
                "parkingSpacesNearby",
                "parkingSpacesByEvent",
                "parkingSpacesByOwner",
                "reservationsByUser",
                "reservationsActive",
                "activeEvents",
                "dashboardStats"
        );
    }
}
