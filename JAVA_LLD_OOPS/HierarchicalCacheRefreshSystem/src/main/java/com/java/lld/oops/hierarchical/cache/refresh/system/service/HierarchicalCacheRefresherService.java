package com.java.lld.oops.hierarchical.cache.refresh.system.service;


import com.java.oops.cache.types.distributed.AbstractDistributedCache;
import com.java.oops.cache.types.ttl.AbstractTTLCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class HierarchicalCacheRefresherService {

    private final LeaderElectionService leaderElectionService;

    /**
     * Refreshes the caches with the given data
     * Only one thread can access this method and update local cache and Only the leader can update the distributed cache.
     *
     * @param key String
     * @param data of type T
     * @param localCache AbstractTTLCache
     * @param distributedCache AbstractCache
     */
    public synchronized <K, V> void refreshCaches(K key, V data, Duration ttl, AbstractTTLCache<K, V> localCache, AbstractDistributedCache<K, V> distributedCache) throws Exception {
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

    public synchronized <K, V> void syncLocalCache(K key, AbstractDistributedCache<K, V> distributedCache, AbstractTTLCache<K, V> localCache, Duration ttl) {
        Optional<V> remoteCacheData = distributedCache.get(key);
        if(remoteCacheData.isPresent()){
            log.info("Updating local cache from distributed cache");
            localCache.put(key, remoteCacheData.get(), ttl);
        } else {
            log.info("No data found in distributed cache");
        }
    }
}
