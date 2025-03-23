package com.java.pubsub.orderprocessing.pubsub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.channel.DirectChannel;

@Configuration
public class PubSubConfig {

    @Bean(name = "pubsubInputChannel")
    public MessageChannel pubsubInputChannel() {
        return new DirectChannel();
    }
}
