package com.java.pubsub.orderprocessing.pubsub;

import com.java.pubsub.orderprocessing.protobuf.OrderProto.Order;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final PubSubTemplate pubSubTemplate;

    public OrderPublisher(PubSubTemplate pubSubTemplate) {
        this.pubSubTemplate = pubSubTemplate;
    }

    public void publishOrder(String topicName, Order order) {

    }
}
