package com.java.java_springintegration.direct_channel.controller;

import com.java.java_springintegration.direct_channel.gateway.OrderGateway;
import com.java.java_springintegration.direct_channel.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RestController
public class OrderController {

    @Autowired
    public OrderGateway orderGateway;

    @PostMapping("/placeOrder")
    public Order placeOrder(@RequestBody Order order) {
        return orderGateway.placeOrder(order).getPayload();
    }
}
