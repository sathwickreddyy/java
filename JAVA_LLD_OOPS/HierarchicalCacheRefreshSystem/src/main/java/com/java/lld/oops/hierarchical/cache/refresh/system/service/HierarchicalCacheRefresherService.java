package com.java.lld.oops.hierarchical.cache.refresh.system.service;


import com.java.oops.cache.types.distributed.AbstractDistributedCache;
import com.java.oops.cache.types.ttl.AbstractTTLCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class HierarchicalCacheRefresherService {

    private final LeaderElectionService leaderElectionService;

    // Per Lock Key Objects for fine-grained synchronization
    private final ConcurrentHashMap<Object, Object> keyLocks = new ConcurrentHashMap<>();

    private Object getLock(Object key){
        // use atomic lock for atomic lock creation.
        return keyLocks.computeIfAbsent(key, k -> new Object());
    }

    /**
     * Refreshes the caches with the given data
     * Only one thread can access this method and update local cache and Only the leader can update the distributed cache.
     *
     * @param key String
     * @param data of type T
     * @param localCache AbstractTTLCache
     * @param distributedCache AbstractCache
     */
    public <K, V> void refreshCaches(K key, V data, Duration ttl, AbstractTTLCache<K, V> localCache, AbstractDistributedCache<K, V> distributedCache) throws Exception {
        localCache.put(key, data, ttl); // updates if exists else creates
        log.info("Updated local cache");
        leaderElectionService.electLeader();
        if(leaderElectionService.isLeader()) {
            try {
                distributedCache.put(key, data, ttl);
                log.info("Updated distributed cache");
            } catch (Exception e) {
                log.error("Exception occurred while updating distributed cache as a LEADER ", e);
                leaderElectionService.cleanUp();
            }
        }
    }

    public <K, V> void refreshCachesV2(
            K key,
            V data,
            Duration ttl,
            AbstractTTLCache<K, V> localCache,
            AbstractDistributedCache<K, V> distributedCache
    ) throws Exception {
        // Per-key locking for local cache update
        Object lock = getLock(key);
        synchronized (lock) {
            // Double-check to avoid redundant updates
            Optional<V> cached = localCache.get(key);
            if (cached.isPresent() && cached.get().equals(data)) {
                log.info("Local cache already up-to-date for key={}", key);
                return;
            }
            localCache.put(key, data, ttl);
            log.info("Updated local cache for key={}", key);

            leaderElectionService.electLeader();
            if (leaderElectionService.isLeader()) {
                try {
                    distributedCache.put(key, data, ttl);
                    log.info("Updated distributed cache for key={}", key);
                } catch (Exception e) {
                    log.error("Exception while updating distributed cache as LEADER for key={}", key, e);
                    leaderElectionService.cleanUp();
                }
            }
        }
    }

    /**
     * Low Performance syncLocalCache - lock on this entire object, only one thread can access this method in the entire object.
     * @param key of type K
     * @param distributedCache of type AbstractDistributedCache
     * @param localCache of type AbstractTTLCache
     * @param ttl of type Duration
     * @param <K> type K
     * @param <V> type V
     */
    public <K, V> void syncLocalCache(K key, AbstractDistributedCache<K, V> distributedCache, AbstractTTLCache<K, V> localCache, Duration ttl) {
        // Only 1st thread will update the local cache
        // Remaining threads which are waiting to update the local cache will wait here and once the local cache is updated, they will
        // not try to update the local cache again
        synchronized (this) {
            Optional<V> localCacheData = localCache.get(key);
            if(localCacheData.isPresent()){
                log.info("Local cache already has data");
                return;
            }
            Optional<V> remoteCacheData = distributedCache.get(key);
            if(remoteCacheData.isPresent()){
                log.info("Updating local cache from distributed cache");
                localCache.put(key, remoteCacheData.get(), ttl);
            } else {
                log.info("No data found in distributed cache");
            }
        }
    }

    /**
     * High Performance Cache Updates - Uses fine-grained locking mechanism for synchronization and double checks to avoid race conditions
     *
     * @param key of type K
     * @param distributedCache of type AbstractDistributedCache
     * @param localCache of type AbstractTTLCache
     * @param ttl of type Duration
     * @param <K> type K
     * @param <V> type V
     */
    public <K, V> void syncLocalCacheV2(K key, AbstractDistributedCache<K, V> distributedCache, AbstractTTLCache<K, V> localCache, Duration ttl) {
        // First check (no lock)
        if(localCache.get(key).isPresent()){
            // return if local cache already has data
            log.info("V2 : Local cache already has data");
            return;
        }
        Object lockKey = getLock(key);
        synchronized (lockKey) {
            Optional<V> localCacheData = localCache.get(key);
            // Second check (with lock)
            if(localCacheData.isPresent()){
                log.info("Local cache already has data after lock");
                return;
            }
            Optional<V> remoteCacheData = distributedCache.get(key);
            if(remoteCacheData.isPresent()){
                log.info("Updating local cache from distributed cache for key={}", key);
                localCache.put(key, remoteCacheData.get(), ttl);
            } else {
                log.info("No data found in distributed cache for key={}", key);
            }
        }
    }
}
