package com.smart.parking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)))
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     RedisCacheConfiguration cacheConfiguration) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("parkingSpaces", cacheConfiguration.entryTtl(Duration.ofSeconds(60)));
        cacheConfigurations.put("parkingSpacesNearby", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("parkingSpacesByEvent", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("parkingSpacesByOwner", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("reservationsByUser", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("reservationsActive", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("activeEvents", cacheConfiguration.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("dashboardStats", cacheConfiguration.entryTtl(Duration.ofSeconds(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
