package org.twins.core.config.filter;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.twins.core.exception.ErrorCodeTwins;

import java.io.IOException;

@RequiredArgsConstructor
public class BulkheadFilter extends OncePerRequestFilter {

    private final BulkheadRegistry bulkheadRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    private Bulkhead bulkhead;
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init() {
        bulkhead = bulkheadRegistry.bulkhead("api");
        rateLimiter = rateLimiterRegistry.rateLimiter("api");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!rateLimiter.acquirePermission()) {
            throw new ServletException(new ServiceException(ErrorCodeTwins.TOO_MANY_REQUESTS, "Rate limit exceeded"));
        }

        if (!bulkhead.tryAcquirePermission()) {
            throw new ServletException(new ServiceException(ErrorCodeTwins.TOO_MANY_REQUESTS, "Too many concurrent requests"));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always release the slot — otherwise a failed request leaks the counter
            // and the bulkhead starts rejecting new requests without real load.
            bulkhead.onComplete();
        }
    }
}
