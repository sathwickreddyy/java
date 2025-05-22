package com.java.lld.oops.hierarchical.cache.refresh.system.test;

import com.java.lld.oops.hierarchical.cache.refresh.system.service.HierarchicalCacheRefresherService;
import com.java.lld.oops.hierarchical.cache.refresh.system.service.LeaderElectionService;
import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint.SampleResponseListWrapper;
import com.java.oops.cache.eviction.LFUEvictionPolicy;
import com.java.oops.cache.types.InMemoryCache;
import com.java.oops.cache.types.distributed.AbstractDistributedCache;
import com.java.oops.cache.types.distributed.RedisDistributedCache;
import com.java.oops.cache.types.ttl.AbstractTTLCache;
import com.java.oops.cache.types.ttl.InMemoryTTLCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.List;

@Configuration
public class SampleConfiguration {

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Jedis jedisClient() { return new Jedis(redisHost, redisPort);}

    @Bean
    public AbstractDistributedCache<String, SampleResponseListWrapper> redisDistributedCache() {
        return new RedisDistributedCache<>(jedisClient());
    }

    @Bean
    public LeaderElectionService leaderElectionService() {
        return LeaderElectionService.builder()
                .leaderKey("leader")
                .lockTTL(Duration.ofMinutes(2))
                .distributedCache(new RedisDistributedCache<>(jedisClient()))
                .build();
    }

    @Bean
    public HierarchicalCacheRefresherService hierarchicalCacheRefresherService() {
        return new HierarchicalCacheRefresherService(leaderElectionService());
    }

    @Bean
    public AbstractTTLCache<String, SampleResponseListWrapper> localCache() {
        return new InMemoryTTLCache<>(new LFUEvictionPolicy<>(), 1000);
    }

    @Bean
    public InMemoryCache<String, List<SampleResponse>> cache() { return new InMemoryCache<>(new LFUEvictionPolicy<>(), 1000); }
}
