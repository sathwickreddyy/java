package com.java.lld.oops.hierarchical.cache.refresh.system.test;

import com.java.lld.oops.hierarchical.cache.refresh.system.service.HierarchicalCacheRefresherService;
import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleDataRestEndpoint;
import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleResponseListWrapper;
import com.java.oops.cache.types.distributed.AbstractDistributedCache;
import com.java.oops.cache.types.ttl.AbstractTTLCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {
    private final SampleDataRestEndpoint restEndpoint;
    private final AbstractDistributedCache<String, SampleResponseListWrapper> distributedCache;
    private final AbstractTTLCache<String, SampleResponseListWrapper> localCache;
    private final HierarchicalCacheRefresherService hierarchicalCacheRefresherService;

    // Metrics counters (could be replaced with Micrometer or Prometheus counters)
    private final AtomicLong cacheHitCount = new AtomicLong();
    private final AtomicLong cacheMissCount = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();

    /**
     * Returns sample responses, using cache and logs performance metrics.
     */
    public List<SampleResponse> getSampleResponseWithCachingSupport(String conditions) throws Exception {
        long startTime = System.nanoTime();
        try {
            // Sync local cache with remote
            hierarchicalCacheRefresherService.syncLocalCacheV2(conditions, distributedCache, localCache, Duration.ofMinutes(30));

            // Check local cache again
            Optional<SampleResponseListWrapper> optionalSampleResponseListWrapper = localCache.get(conditions);
            if (optionalSampleResponseListWrapper.isPresent()) {
                cacheHitCount.incrementAndGet();
                logPerformance("getSampleResponseWithCachingSupport", startTime, true, null);
                return optionalSampleResponseListWrapper.get().getSampleResponseList();
            }

            // Cache miss: fetch from endpoint and refresh caches
            cacheMissCount.incrementAndGet();
            SampleResponseListWrapper sampleResponse = restEndpoint.getSampleData();
            hierarchicalCacheRefresherService.refreshCachesV2(conditions, sampleResponse, Duration.ofHours(1), localCache, distributedCache);

            logPerformance("getSampleResponseWithCachingSupport", startTime, false, null);
            return sampleResponse.getSampleResponseList();
        } catch (Exception ex) {
            errorCount.incrementAndGet();
            logPerformance("getSampleResponseWithCachingSupport", startTime, false, ex);
            throw ex;
        }
    }

    /**
     * Logs performance metrics for method execution.
     */
    private void logPerformance(String method, long startTime, boolean cacheHit, Exception ex) {
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        if (ex == null) {
            log.info("Method={} duration={}ms cacheHit={} cacheHitCount={} cacheMissCount={}",
                    method, durationMs, cacheHit, cacheHitCount.get(), cacheMissCount.get());
        } else {
            log.error("Method={} duration={}ms cacheHit={} errorCount={} Exception={}",
                    method, durationMs, cacheHit, errorCount.get(), ex.getMessage());
        }
    }

    /**
     * Returns current performance statistics.
     */
    public PerformanceStats getPerformanceStats() {
        return new PerformanceStats(
                cacheHitCount.get(),
                cacheMissCount.get(),
                errorCount.get()
        );
    }

    /**
     * Simple POJO for performance stats.
     */
    @Data
    @AllArgsConstructor
    public static class PerformanceStats {
        private long cacheHits;
        private long cacheMisses;
        private long errors;
    }

    // Optionally, add methods to reset metrics for benchmarking
    public void resetPerformanceStats() {
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        errorCount.set(0);
    }
}
