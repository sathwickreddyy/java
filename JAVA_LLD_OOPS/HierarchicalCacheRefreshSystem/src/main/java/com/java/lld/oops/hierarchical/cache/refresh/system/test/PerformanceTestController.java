package com.java.lld.oops.hierarchical.cache.refresh.system.test;

import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PerformanceTestController {

    private final TestService testService;

    /**
     * Endpoint to fetch sample responses with caching support.
     * @param key The cache key (simulate different or same keys for different scenarios).
     */
    @GetMapping("/test-cache/{key}")
    public List<SampleResponse> testCache(@PathVariable String key) throws Exception {
        return testService.getSampleResponseWithCachingSupport(key);
    }

    /**
     * Endpoint to fetch current performance stats.
     */
    @GetMapping("/test-cache/stats")
    public TestService.PerformanceStats getStats() {
        return testService.getPerformanceStats();
    }

    /**
     * Endpoint to reset stats before a new test.
     */
    @PostMapping("/test-cache/reset")
    public void resetStats() {
        testService.resetPerformanceStats();
    }
}