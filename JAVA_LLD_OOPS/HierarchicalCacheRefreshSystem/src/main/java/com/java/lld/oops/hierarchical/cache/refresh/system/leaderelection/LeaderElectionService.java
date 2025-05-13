package com.java.lld.oops.hierarchical.cache.refresh.system.leaderelection;

import com.java.lld.oops.hierarchical.cache.refresh.system.utils.SystemIdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderElectionService {
    private final RedisDistributedLock redisDistributedLock;
    private static final String LEADER_KEY = "leader";
    private final Jedis jedisClient;

    public String getCurrentLeader() {
        log.info("Fetching current leader");
        return jedisClient.get(LEADER_KEY);
    }

    public boolean isLeader() {
        String currentLeader = getCurrentLeader();
        log.info("Current leader is {}", currentLeader);
        return currentLeader != null && currentLeader.equals(SystemIdentifierUtil.getSystemUniqueKey());
    }

    public void becomeLeader() {
        String currentLeader = getCurrentLeader();
        if(currentLeader != null) {
            log.info("Leader already elected, current leader is {}", currentLeader);
            return;
        }
        log.info("Trying to become leader");
        redisDistributedLock.acquireLock(LEADER_KEY, SystemIdentifierUtil.getSystemUniqueKey(), 60000);
        log.info("{} Became leader!", SystemIdentifierUtil.getSystemUniqueKey());
    }

    public void electLeader() {
        String currentLeader = getCurrentLeader();
        if(currentLeader != null) { return; }
        becomeLeader();
    }

    public void cleanUp() {
        redisDistributedLock.releaseLock(LEADER_KEY, SystemIdentifierUtil.getSystemUniqueKey());
    }
}
