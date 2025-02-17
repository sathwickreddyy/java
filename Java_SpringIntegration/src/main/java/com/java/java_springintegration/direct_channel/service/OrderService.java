package com.java.java_springintegration.direct_channel.service;

import com.java.java_springintegration.direct_channel.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

    @ServiceActivator(inputChannel = "request-in-channel", outputChannel = "order-process-channel")
    public Message<Order> placeOrder(Message<Order> order) {
        log.info("Order Received, Processing Order ...");
        return order;
    }

    @ServiceActivator(inputChannel = "order-process-channel", outputChannel = "order-reply-channel")
    public Message<Order> processOrder(Message<Order> order) {
        log.info("Order Processed, Placing Order ...");
        order.getPayload().setOrderStatus("Order Successfully Placed !!!");
        return order;
    }

    @ServiceActivator(inputChannel = "order-reply-channel")
    public void replyOrder(Message<Order> order)
    {
        log.info("Responding to client that -> Order Placed Successfully !!!");
        MessageChannel replyChannel = (MessageChannel) order.getHeaders().getReplyChannel();
        replyChannel.send(order);
    }
}
