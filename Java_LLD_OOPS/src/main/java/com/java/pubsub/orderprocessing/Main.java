package com.java.pubsub.orderprocessing;


import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import com.java.pubsub.orderprocessing.pubsub.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.util.Random;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private OrderProducer orderProducer;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String topic = "orders-topic";
        Random random = new Random();
        String orderId = "order-" + random.nextInt(1000);
        String productId = "product-" + random.nextInt(1000);

        Instant instant = Instant.now();
        long currentTimestamp = instant.toEpochMilli();

        OrderProto.Order order = OrderProto.Order.newBuilder()
                        .setOrderId(orderId)
                        .setCustomerName("Bunny Reddy")
                        .setProductId(productId)
                        .setQuantity(2)
                        .setTimestamp(currentTimestamp)
                        .build();
        orderProducer.sendOrder(topic, order);
    }
}