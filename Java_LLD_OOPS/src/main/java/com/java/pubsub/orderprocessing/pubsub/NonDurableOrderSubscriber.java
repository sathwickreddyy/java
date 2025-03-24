package com.java.pubsub.orderprocessing.pubsub;

import com.java.pubsub.orderprocessing.protobuf.OrderProto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

/**
 * NonDurableOrderSubscriber processes transient messages received via Spring Integration.
 */
@Slf4j
@Component
public class NonDurableOrderSubscriber implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof Order)) {
            log.warn("Unexpected payload type: {}", message.getPayload().getClass().getName());
            return;
        }

        Order order = (Order) message.getPayload();
        log.info("Processed non-durable order: orderId={}, data={}", order.getOrderId(), order);
    }
}