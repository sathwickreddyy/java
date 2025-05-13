package com.java.lld.oops.hierarchical.cache.refresh.system.configuration;

import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponseListWrapper;
import com.java.oops.cache.eviction.LFUEvictionPolicy;
import com.java.oops.cache.types.InMemoryCache;
import com.java.oops.cache.types.RedisDistributedCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

import java.util.List;

@Configuration
public class SampleConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Jedis jedisClient() { return new Jedis("localhost", 6379);}

    @Bean
    public RedisDistributedCache<String, SampleResponseListWrapper> redisDistributedCache() {
        return new RedisDistributedCache<>(jedisClient());
    }

    @Bean
    public InMemoryCache<String, List<SampleResponse>> cache() { return new InMemoryCache<>(new LFUEvictionPolicy<>(), 1000); }
}
