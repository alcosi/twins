package org.twins.core.unit.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.config.filter.BulkheadFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkheadFilterTest extends BaseUnitTest {

    private static final int MAX_CONCURRENT_CALLS = 50;
    private static final int RATE_LIMIT = 200;

    private BulkheadFilter bulkheadFilter;

    @BeforeEach
    void setUp() {
        var bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(MAX_CONCURRENT_CALLS)
                .maxWaitDuration(Duration.ZERO)
                .build();
        var bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);

        var rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(RATE_LIMIT)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();
        var rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);

        bulkheadFilter = new BulkheadFilter(bulkheadRegistry, rateLimiterRegistry);
        bulkheadFilter.init();
    }

    @Test
    void doFilter_apiRequest_chainCalled() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/test");
        var response = new MockHttpServletResponse();
        var chainCalled = new AtomicInteger(0);

        bulkheadFilter.doFilter(request, response, (req, res) -> chainCalled.incrementAndGet());

        assertEquals(1, chainCalled.get());
    }

    @Nested
    class ActuatorBypass {

        @Test
        void doFilter_actuatorRequest_chainCalled() throws ServletException, IOException {
            var request = new MockHttpServletRequest("GET", "/actuator/health");
            var response = new MockHttpServletResponse();
            var chainCalled = new AtomicInteger(0);

            bulkheadFilter.doFilter(request, response, (req, res) -> chainCalled.incrementAndGet());

            assertEquals(1, chainCalled.get());
            assertEquals(200, response.getStatus());
        }

        @Test
        void doFilter_actuatorRequest_doesNotConsumeBulkheadSlot() {
            var barrier = new CyclicBarrier(MAX_CONCURRENT_CALLS);
            var actuatorCalled = new AtomicInteger(0);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
                    executor.submit(() -> {
                        try {
                            bulkheadFilter.doFilter(
                                    new MockHttpServletRequest("GET", "/api/test"),
                                    new MockHttpServletResponse(),
                                    (req, res) -> {
                                        try {
                                            barrier.await(5, TimeUnit.SECONDS);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );
                        } catch (Exception ignored) {
                        }
                    });
                }

                try {
                    bulkheadFilter.doFilter(
                            new MockHttpServletRequest("GET", "/actuator/health"),
                            new MockHttpServletResponse(),
                            (req, res) -> actuatorCalled.incrementAndGet()
                    );
                } catch (Exception ignored) {
                }
            }

            assertEquals(1, actuatorCalled.get(), "Actuator should bypass bulkhead even when slots are full");
        }
    }

    @Nested
    class ConcurrentRequests {

        @Test
        void doFilter_withinLimit_allPass() {
            var barrier = new CyclicBarrier(MAX_CONCURRENT_CALLS);
            var successCount = new AtomicInteger(0);
            var failCount = new AtomicInteger(0);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
                    executor.submit(() -> {
                        try {
                            bulkheadFilter.doFilter(
                                    new MockHttpServletRequest("GET", "/api/test"),
                                    new MockHttpServletResponse(),
                                    (req, res) -> {
                                        try {
                                            barrier.await(5, TimeUnit.SECONDS);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    });
                }
            }

            assertEquals(MAX_CONCURRENT_CALLS, successCount.get());
            assertEquals(0, failCount.get());
        }

        @Test
        void doFilter_exceedingLimit_excessRejected() throws InterruptedException {
            var threadCount = MAX_CONCURRENT_CALLS + 5;
            var startBarrier = new CyclicBarrier(threadCount);
            var holdChain = new CountDownLatch(1);
            var successCount = new AtomicInteger(0);
            var rejectedCount = new AtomicInteger(0);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            startBarrier.await(5, TimeUnit.SECONDS);
                            bulkheadFilter.doFilter(
                                    new MockHttpServletRequest("GET", "/api/test"),
                                    new MockHttpServletResponse(),
                                    (req, res) -> {
                                        try {
                                            holdChain.await(5, TimeUnit.SECONDS);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );
                            successCount.incrementAndGet();
                        } catch (ServletException e) {
                            if (e.getCause() != null && e.getCause().getMessage().contains("Too many concurrent requests"))
                                rejectedCount.incrementAndGet();
                        } catch (Exception ignored) {
                        }
                    });
                }

                Thread.sleep(500);
                holdChain.countDown();
            }

            assertEquals(MAX_CONCURRENT_CALLS, successCount.get());
            assertEquals(5, rejectedCount.get());
        }
    }

    @Nested
    class SlotRelease {

        @Test
        void doFilter_afterCompletion_slotReleased() throws ServletException, IOException {
            for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
                bulkheadFilter.doFilter(
                        new MockHttpServletRequest("GET", "/api/test"),
                        new MockHttpServletResponse(),
                        (req, res) -> {}
                );
            }

            var called = new AtomicInteger(0);
            bulkheadFilter.doFilter(
                    new MockHttpServletRequest("GET", "/api/test"),
                    new MockHttpServletResponse(),
                    (req, res) -> called.incrementAndGet()
            );

            assertEquals(1, called.get());
        }

        @Test
        void doFilter_chainThrows_slotStillReleased() {
            for (int i = 0; i < MAX_CONCURRENT_CALLS; i++) {
                try {
                    bulkheadFilter.doFilter(
                            new MockHttpServletRequest("GET", "/api/test"),
                            new MockHttpServletResponse(),
                            (req, res) -> { throw new RuntimeException("simulated error"); }
                    );
                } catch (Exception ignored) {
                }
            }

            assertDoesNotThrow(() -> bulkheadFilter.doFilter(
                    new MockHttpServletRequest("GET", "/api/test"),
                    new MockHttpServletResponse(),
                    (req, res) -> {}
            ));
        }
    }

    @Nested
    class RateLimiter {

        @Test
        void doFilter_exceedingRateLimit_excessRejected() {
            var rateLimiterConfig = RateLimiterConfig.custom()
                    .limitForPeriod(3)
                    .limitRefreshPeriod(Duration.ofSeconds(10))
                    .timeoutDuration(Duration.ZERO)
                    .build();
            var bulkheadConfig = BulkheadConfig.custom()
                    .maxConcurrentCalls(100)
                    .maxWaitDuration(Duration.ZERO)
                    .build();
            var rateLimitedFilter = new BulkheadFilter(
                    BulkheadRegistry.of(bulkheadConfig),
                    RateLimiterRegistry.of(rateLimiterConfig)
            );
            rateLimitedFilter.init();

            var rejected = new AtomicInteger(0);
            for (int i = 0; i < 10; i++) {
                try {
                    rateLimitedFilter.doFilter(
                            new MockHttpServletRequest("GET", "/api/test"),
                            new MockHttpServletResponse(),
                            (req, res) -> {}
                    );
                } catch (Exception e) {
                    if (e.getCause() != null && e.getCause().getMessage().contains("Rate limit exceeded"))
                        rejected.incrementAndGet();
                }
            }

            assertEquals(7, rejected.get());
        }
    }
}
