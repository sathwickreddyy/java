package com.java.oops.cache.eviction;

/**
 * LFU (Least Frequently Used) Cache Implementation
 *
 * <pre>
 * This cache removes the least frequently used item when it reaches capacity.
 * It ensures O(1) operations for get() and put() using:
 * - HashMap for storing key-value pairs.
 * - HashMap for tracking access frequency.
 * - LinkedHashSet to maintain order within the same frequency.
 * </pre>
 *
 * <h2>LRU vs. LFU: Key Differences</h2>
 *
 * <table border="1">
 * <tr><th>Feature</th><th>LRU (Least Recently Used)</th><th>LFU (Least Frequently Used)</th></tr>
 * <tr><td><b>Eviction Rule</b></td><td>Removes the least recently accessed item</td><td>Removes the least frequently accessed item</td></tr>
 * <tr><td><b>Tracking Mechanism</b></td><td>Last access time</td><td>Access count</td></tr>
 * <tr><td><b>Data Structures</b></td><td>LinkedHashMap or Doubly Linked List + HashMap</td><td>Min-Heap + HashMap or TreeMap</td></tr>
 * <tr><td><b>Best Use Case</b></td><td>Useful when recent access is important (e.g., web browser cache)</td><td>Useful when frequently accessed items should be retained (e.g., database query cache)</td></tr>
 * <tr><td><b>Time Complexity</b></td><td>O(1) for get() and put() using LinkedHashMap</td><td>O(log n) for put() due to heap reordering</td></tr>
 * </table>
 *
 * @param <K> Type of key used in the cache
 */
public class LFUEvictionPolicy<K> implements EvictionPolicy<K> {
    /**
     * Records the access of the key
     *
     * @param key of type K
     */
    @Override
    public void recordAccess(K key) {

    }

    /**
     * Evicts the key
     *
     * @return Key of type K
     */
    @Override
    public K evict() {
        return null;
    }
}
