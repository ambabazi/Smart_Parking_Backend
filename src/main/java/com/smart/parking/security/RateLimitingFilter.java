package com.smart.parking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT_PER_MINUTE = 12;
    private static final int PUBLIC_LIMIT_PER_MINUTE = 60;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isRateLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = buildKey(request);
        long limit = isAuthPath(request) ? AUTH_LIMIT_PER_MINUTE : PUBLIC_LIMIT_PER_MINUTE;
        long count = increment(bucketKey);

        if (count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private long increment(String bucketKey) {
        long now = Instant.now().getEpochSecond();
        Window window = windows.compute(bucketKey, (key, existing) -> {
            if (existing == null || existing.expiresAtEpochSecond() <= now) {
                return new Window(now + 60, 1);
            }
            return new Window(existing.expiresAtEpochSecond(), existing.count() + 1);
        });
        return window.count();
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
        return ip + ":" + request.getRequestURI();
    }

    private record Window(long expiresAtEpochSecond, long count) {}
}
