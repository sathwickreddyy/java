package com.java.pubsub.orderprocessing.pubsub;

import com.java.oops.cache.types.AbstractCache;
import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

/**
 * DurableOrderSubscriber processes orders received via Spring Integration,
 * storing them in a local cache to ensure durability and avoid duplication.
 */
@Slf4j
@Component("durableOrderSubscriber")
public class DurableOrderSubscriber implements MessageHandler {
    private final AbstractCache<String, OrderProto.Order> orderCache;

    public DurableOrderSubscriber(AbstractCache<String, OrderProto.Order> orderCache) {
        this.orderCache = orderCache;
    }

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderProto.Order)) {
            log.warn("Unexpected payload type: {}", message.getPayload().getClass().getName());
            return;
        }

        OrderProto.Order order = (OrderProto.Order) message.getPayload();
        String orderId = order.getOrderId();

        if (orderCache.get(orderId).isEmpty()) {
            orderCache.put(orderId, order);
            log.info("Cached durable order: {}", orderId);
        } else {
            log.debug("Order {} already exists in cache", orderId);
        }
    }
}
