package org.twins.core.config.filter;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BulkheadFilterLoadTest {

    private static final int MAX_CONCURRENT_CALLS = 50;
    private static final int RATE_LIMIT = 200;

    private BulkheadFilter bulkheadFilter;

    @BeforeEach
    void setUp() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(MAX_CONCURRENT_CALLS)
                .maxWaitDuration(Duration.ZERO)
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);

        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(RATE_LIMIT)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);

        bulkheadFilter = new BulkheadFilter(bulkheadRegistry, rateLimiterRegistry);
        bulkheadFilter.init();
    }

    @Test
    void singleRequest_shouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalled = new AtomicInteger(0);

        FilterChain chain = (req, res) -> chainCalled.incrementAndGet();

        bulkheadFilter.doFilterInternal(request, response, chain);

        assertEquals(1, chainCalled.get());
    }

    @Test
    void actuatorRequest_shouldBeSkipped() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(bulkheadFilter.shouldNotFilter(request));
    }

    @Test
    void concurrentRequests_withinLimit_shouldAllPass() throws InterruptedException {
        int threadCount = MAX_CONCURRENT_CALLS;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    FilterChain chain = (req, res) -> {
                        try {
                            barrier.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    bulkheadFilter.doFilterInternal(request, response, chain);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(threadCount, successCount.get(), "All requests within limit should succeed");
        assertEquals(0, failCount.get());
    }

    @Test
    void concurrentRequests_exceedingLimit_shouldReject() throws Exception {
        int threadCount = MAX_CONCURRENT_CALLS + 5;
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        CountDownLatch holdChain = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startBarrier.await(5, TimeUnit.SECONDS);
                    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    FilterChain chain = (req, res) -> {
                        try {
                            holdChain.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    bulkheadFilter.doFilterInternal(request, response, chain);
                    successCount.incrementAndGet();
                } catch (ServletException e) {
                    if (e.getCause() != null && e.getCause().getMessage().contains("Too many concurrent requests")) {
                        rejectedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // unexpected
                }
            });
        }

        // Wait for rejected threads to get their rejections, then release successful ones
        Thread.sleep(500);
        holdChain.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(MAX_CONCURRENT_CALLS, successCount.get(),
                "Only " + MAX_CONCURRENT_CALLS + " requests should succeed");
        assertEquals(5, rejectedCount.get(), "Excess requests should be rejected by bulkhead");
    }

    @Test
    void afterCompletion_bulkheadSlotIsReleased() throws ServletException, IOException {
        // Fill all slots and release them
        for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();
            bulkheadFilter.doFilterInternal(request, response, (req, res) -> {});
        }

        // Should still work — slots were released in finally block
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger called = new AtomicInteger(0);
        bulkheadFilter.doFilterInternal(request, response, (req, res) -> called.incrementAndGet());
        assertEquals(1, called.get(), "Slot should be available after previous requests completed");
    }

    @Test
    void chainException_shouldStillReleaseBulkheadSlot() {
        // A request that throws inside the chain should still release the bulkhead slot
        for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
            try {
                MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
                MockHttpServletResponse response = new MockHttpServletResponse();
                bulkheadFilter.doFilterInternal(request, response, (req, res) -> {
                    throw new RuntimeException("simulated error");
                });
            } catch (Exception ignored) {
            }
        }

        // After all failing requests, slots should still be available
        assertDoesNotThrow(() -> {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();
            bulkheadFilter.doFilterInternal(request, response, (req, res) -> {});
        }, "Bulkhead slot should be released even when chain throws");
    }

    @Test
    void rateLimiter_exceedingLimit_shouldReject() {
        AtomicInteger rejected = new AtomicInteger(0);

        // Set up filter with very low rate limit for this test
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(3)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);

        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(100)
                .maxWaitDuration(Duration.ZERO)
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);

        BulkheadFilter rateLimitedFilter = new BulkheadFilter(bulkheadRegistry, rateLimiterRegistry);
        rateLimitedFilter.init();

        for (int i = 0; i < 10; i++) {
            try {
                MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
                MockHttpServletResponse response = new MockHttpServletResponse();
                rateLimitedFilter.doFilterInternal(request, response, (req, res) -> {});
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("Rate limit exceeded")) {
                    rejected.incrementAndGet();
                }
            }
        }

        assertEquals(7, rejected.get(), "7 out of 10 requests should be rate-limited");
    }
}