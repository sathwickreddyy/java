package com.java.java_springintegration.queue_channel.controller;

import com.java.java_springintegration.queue_channel.gateway.OrderingGateway;
import com.java.java_springintegration.queue_channel.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController("orderController2")
@RequestMapping("/api/v2/orders")
public class OrderController {

    @Autowired
    private OrderingGateway orderGateway;

    @PostMapping("/placeBulkOrder")
    public List<Order> placeOrder(@RequestBody List<Order> orders) {
        List<CompletableFuture<Order>> futures = orders.stream()
                .map(order -> CompletableFuture.supplyAsync(() -> orderGateway.placeOrder(order).getPayload()))
                .toList();
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }
}

/**
 * Request :
 * POST http://localhost:8080/api/v2/orders/placeBulkOrder
 * Content-Type: application/json
 *
 * [
 *   {
 *     "orderId": 20,
 *     "itemName": "Veg Biryani",
 *     "amount": 240,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 21,
 *     "itemName": "Mutton Biryani",
 *     "amount": 260,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 15,
 *     "itemName": "Sweet Lassi",
 *     "amount": 90,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 22,
 *     "itemName": "Chicken Tikka",
 *     "amount": 180,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 23,
 *     "itemName": "Palak Paneer",
 *     "amount": 200,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 24,
 *     "itemName": "Garlic Naan",
 *     "amount": 60,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 25,
 *     "itemName": "Mixed Veg Curry",
 *     "amount": 220,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 26,
 *     "itemName": "Raita",
 *     "amount": 50,
 *     "orderStatus": ""
 *   },
 *   {
 *     "orderId": 27,
 *     "itemName": "Tandoori Chicken",
 *     "amount": 280,
 *     "orderStatus": ""
 *   }
 * ]
 *
 */