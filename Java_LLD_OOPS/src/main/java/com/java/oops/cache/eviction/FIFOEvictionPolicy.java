package com.java.oops.cache.eviction;

import lombok.extern.slf4j.Slf4j;
import java.util.LinkedList;
import java.util.Queue;

/**
 * FIFO (First-In-First-Out) Eviction Policy implementation.
 *
 * <p>This policy evicts keys in the exact order they were added,
 * regardless of their access frequency or recency.</p>
 *
 * @param <K> Key type.
 */
@Slf4j
public class FIFOEvictionPolicy<K> implements EvictionPolicy<K> {

    /**
     * Queue to maintain insertion order of keys.
     */
    private final Queue<K> queue;

    /**
     * Constructs a FIFO Eviction Policy instance.
     */
    public FIFOEvictionPolicy() {
        this.queue = new LinkedList<>();
        log.info("Initialized FIFO Eviction Policy");
    }

    /**
     * Records the access of the key.
     *
     * <p>In FIFO policy, repeated access does not affect eviction priority.
     * The key is only recorded if it's new to the cache.</p>
     *
     * @param key Key accessed.
     */
    @Override
    public synchronized void recordAccess(K key) {
        if (!queue.contains(key)) {
            queue.offer(key);
            log.debug("Key recorded: {}. Current queue state: {}", key, queue);
        } else {
            log.debug("Key {} already present. No action taken.", key);
        }
    }

    /**
     * Evicts the oldest inserted key from the cache.
     *
     * @return Evicted key or null if no keys are available.
     */
    @Override
    public synchronized K evict() {
        K evictedKey = queue.poll();
        if (evictedKey == null) {
            log.warn("Eviction requested but cache is empty");
            return null;
        }
        log.info("Evicted oldest inserted key: {}", evictedKey);
        return evictedKey;
    }
}
