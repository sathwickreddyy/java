package com.java.lld.oops.hierarchical.cache.refresh.system.service;

import com.java.lld.oops.hierarchical.cache.refresh.system.utils.SystemIdentifierUtil;
import com.java.oops.cache.types.distributed.AbstractDistributedCache;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public class LeaderElectionService {
    private final String lockKey;
    private final Duration lockTTL;
    private final AbstractDistributedCache<String, String> distributedCache;
    private static final String UNIQUE_KEY = SystemIdentifierUtil.getSystemUniqueKey();

    public LeaderElectionService(String leaderKey, Duration lockTTL, AbstractDistributedCache<String, String> distributedCache) {
        this.lockKey = distributedCache.getLockKey(leaderKey);
        this.lockTTL = lockTTL;
        this.distributedCache = distributedCache;
    }


    public Optional<String> getCurrentLeader() {
        log.info("Fetching current leader");
        try {
            return Optional.ofNullable(distributedCache.fetchLockValue(lockKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLeader() {
        Optional<String> currentLeader = getCurrentLeader();
        if(currentLeader.isPresent()) {
            log.info("Current leader is {}", currentLeader);
            return currentLeader.get().equals(UNIQUE_KEY);
        }
        return false;
    }

    public void becomeLeader() throws Exception {
        Optional<String> currentLeader = getCurrentLeader();
        if(currentLeader.isPresent()) {
            log.info("Leader already elected, current leader is {}", currentLeader);
            return;
        }
        log.info("Attempting to become leader");
        distributedCache.acquireLock(lockKey, UNIQUE_KEY, lockTTL);
        log.info("{} Became leader!", UNIQUE_KEY);
    }

    public void electLeader() throws Exception {
        Optional<String> currentLeader = getCurrentLeader();
        if(currentLeader.isPresent()) { return; }
        becomeLeader();
    }

    public void cleanUp() throws Exception {
        distributedCache.releaseLock(lockKey);
    }
}
