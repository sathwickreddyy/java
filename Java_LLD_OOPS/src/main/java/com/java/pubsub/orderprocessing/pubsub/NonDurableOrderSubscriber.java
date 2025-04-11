package com.java.pubsub.orderprocessing.pubsub;

import com.java.pubsub.orderprocessing.protobuf.OrderProto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void printBatchMessages(Message<?> message) {
        if (!(message.getPayload() instanceof List<?> list)) {
            log.warn("Unexpected payload type: {}", message.getPayload().getClass().getName());
            return;
        }

        if (list.isEmpty() || !(list.get(0) instanceof Order)) {
            log.warn("List doesn't contain OrderProto.Order objects");
            return;
        }

        List<Order> orders = (List<Order>) list;
        log.info("Received {} non-durable orders", orders.size());
        orders.forEach(order -> log.info("Processed non-durable order: orderId={}, data={}", order.getOrderId(), order));
    }

}