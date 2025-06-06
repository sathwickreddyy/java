package com.java.lld.oops.hierarchical.cache.refresh.system.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PerformanceIntegrationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    private static final int NUM_THREADS = 1; // Number of concurrent threads
    private static final int NUM_REQUESTS = 10; // Total requests (adjust as needed)

    @BeforeEach
    void resetStats() {
        restTemplate.postForEntity("/test-cache/reset", null, Void.class);
    }

    @Test
    void scenarioA_allRequestsSameKey() throws Exception {
        String key = "sameKey";
        runConcurrentRequests(key, NUM_REQUESTS);

        // Fetch and print metrics
        ResponseEntity<PerformanceStats> stats = restTemplate.getForEntity("/test-cache/stats", PerformanceStats.class);
        System.out.println("Scenario A Stats: " + stats.getBody());
    }

    @Test
    void scenarioB_manyUniqueKeys() throws Exception {
        runConcurrentRequestsWithUniqueKeys(NUM_REQUESTS);

        // Fetch and print metrics
        ResponseEntity<PerformanceStats> stats = restTemplate.getForEntity("/test-cache/stats", PerformanceStats.class);
        System.out.println("Scenario B Stats: " + stats.getBody());
    }

    private void runConcurrentRequests(String key, int numRequests) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(numRequests);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numRequests; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    restTemplate.getForEntity("/test-cache/"+key, List.class, key);
                } catch (Exception e) {
                    // Handle error if needed
                }
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                responseTimes.add(durationMs);
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        printResponseStats(responseTimes, "Scenario A");
    }

    private void runConcurrentRequestsWithUniqueKeys(int numRequests) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(numRequests);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numRequests; i++) {
            final String key = "key_" + i;
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    restTemplate.getForEntity("/test-cache?key={key}", List.class, key);
                } catch (Exception e) {
                    // Handle error if needed
                }
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                responseTimes.add(durationMs);
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        printResponseStats(responseTimes, "Scenario B");
    }

    private void printResponseStats(List<Long> responseTimes, String scenario) {
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        System.out.printf("%s - Avg: %.2f ms, Min: %d ms, Max: %d ms, Total: %d%n",
                scenario, avg, min, max, responseTimes.size());
    }

    // DTO for stats (should match your TestService.PerformanceStats)
    public static class PerformanceStats {
        public long cacheHits;
        public long cacheMisses;
        public long errors;

        @Override
        public String toString() {
            return String.format("hits=%d, misses=%d, errors=%d", cacheHits, cacheMisses, errors);
        }
    }
}
