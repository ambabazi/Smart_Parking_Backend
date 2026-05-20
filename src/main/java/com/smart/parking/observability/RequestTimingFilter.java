package com.smart.parking.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestTimingFilter extends OncePerRequestFilter {

    @Value("${app.performance.slow-request-threshold-ms:500}")
    private long slowRequestThresholdMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            response.setHeader("X-Response-Time-Ms", String.valueOf(elapsed));
            if (elapsed >= slowRequestThresholdMs) {
                log.warn("Slow request {} {} took {} ms", request.getMethod(), request.getRequestURI(), elapsed);
            }
        }
    }
}
