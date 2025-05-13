package com.java.lld.oops.hierarchical.cache.refresh.system.leaderelection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RedisDistributedLock {
    private final Jedis jedisClient;

    public boolean acquireLock(String lockKey, String lockValue, int ttlMillis)
    {
        // SET key value NX PX expireMillis ensures atomic lock acquisition
        String result = jedisClient.set(lockKey, lockValue, SetParams.setParams().nx().px(ttlMillis));
        return "OK".equals(result);
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        String luaScript = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """;
        Object result = jedisClient.eval(luaScript, Collections.singletonList(lockKey), Collections.singletonList(lockValue));
        return Long.valueOf(1L).equals(result);
    }
}
