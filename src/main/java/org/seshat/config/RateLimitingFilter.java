package org.seshat.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter implements Filter {

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if ("POST".equalsIgnoreCase(req.getMethod()) && "/login".equals(req.getRequestURI())) {
            String ip = req.getRemoteAddr();
            long now = System.currentTimeMillis();
            RateLimitEntry entry = attempts.compute(ip, (key, val) -> {
                if (val == null || now - val.windowStart > WINDOW_MS) {
                    return new RateLimitEntry(now);
                }
                val.count.incrementAndGet();
                return val;
            });
            if (entry.count.get() > MAX_ATTEMPTS) {
                resp.setStatus(429);
                resp.setContentType("text/plain");
                resp.getWriter().write("Demasiados intentos. Intente nuevamente en 1 minuto.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(1);
        RateLimitEntry(long windowStart) { this.windowStart = windowStart; }
    }
}
