package com.java.java_springintegration.direct_channel.gateway;

import com.java.java_springintegration.direct_channel.model.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

@MessagingGateway
public interface OrderGateway {
    @Gateway(requestChannel = "request-in-channel")
    // Send a Order message and return the Order Message Response
    public Message<Order> placeOrder(Order order);
}
