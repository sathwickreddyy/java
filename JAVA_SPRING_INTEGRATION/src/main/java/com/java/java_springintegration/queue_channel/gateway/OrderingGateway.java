package com.java.java_springintegration.queue_channel.gateway;

import com.java.java_springintegration.queue_channel.model.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

@MessagingGateway
public interface OrderingGateway {
    @Gateway(requestChannel = "request-in-channel")
    public Message<Order> placeOrder(Order order);
}
