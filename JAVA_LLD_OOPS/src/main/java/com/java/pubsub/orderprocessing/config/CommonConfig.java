package com.java.pubsub.orderprocessing.config;

import com.java.oops.cache.eviction.EvictionPolicy;
import com.java.oops.cache.eviction.LFUEvictionPolicy;
import com.java.oops.cache.types.AbstractCache;
import com.java.oops.cache.types.InMemoryCache;
import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Bean
    public AbstractCache<String, OrderProto.Order> orderCache() {
        EvictionPolicy<String> evictionPolicy = new LFUEvictionPolicy<>();
        return new InMemoryCache<>(evictionPolicy, 1024);
    } // <1>
}
