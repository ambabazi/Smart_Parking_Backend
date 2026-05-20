package com.smart.parking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT_PER_MINUTE = 12;
    private static final int PUBLIC_LIMIT_PER_MINUTE = 60;
    private static final String AUTH_PREFIX = "rate:auth:";
    private static final String PUBLIC_PREFIX = "rate:public:";

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isRateLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = buildKey(request);
        Long count = redisTemplate.opsForValue().increment(bucketKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(bucketKey, Duration.ofMinutes(1));
        }

        long limit = isAuthPath(request) ? AUTH_LIMIT_PER_MINUTE : PUBLIC_LIMIT_PER_MINUTE;
        if (count != null && count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return isAuthPath(request)
                || path.startsWith("/parking-spaces")
                || path.equals("/events/active");
    }

    private boolean isAuthPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth");
    }

    private String buildKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        String prefix = isAuthPath(request) ? AUTH_PREFIX : PUBLIC_PREFIX;
        return prefix + ip + ":" + request.getRequestURI();
    }
}
