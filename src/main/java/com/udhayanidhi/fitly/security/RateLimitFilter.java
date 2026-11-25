package com.udhayanidhi.fitly.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000;

    // ip -> {windowStart, count}
    private final Map<String, long[]> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && request.getRequestURI().startsWith("/api/auth/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        if (hits.size() > 10_000) {
            hits.entrySet().removeIf(e -> now - e.getValue()[0] > WINDOW_MS);
        }
        long[] entry = hits.compute(request.getRemoteAddr(), (ip, v) -> {
            if (v == null || now - v[0] > WINDOW_MS) return new long[]{now, 1};
            v[1]++;
            return v;
        });
        if (entry[1] > MAX_ATTEMPTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many attempts. Try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}