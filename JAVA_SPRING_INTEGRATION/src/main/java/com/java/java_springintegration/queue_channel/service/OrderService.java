package com.java.java_springintegration.queue_channel.service;

import com.java.java_springintegration.queue_channel.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service("orderService2")
public class OrderService {

    @Bean(name="order-process-channel")
    public MessageChannel orderProcessChannel() {
        return new QueueChannel(10);
    }

    @Bean(name="order-reply-channel")
    public MessageChannel orderReplyChannel() {
        return new QueueChannel(10);
    }

    @ServiceActivator(inputChannel = "request-in-channel", outputChannel = "order-process-channel")
    public Message<Order> placeOrder(Message<Order> order) {
        log.info("Order Received, Processing Order ... {}", order.getPayload());
        return order;
    }

    @ServiceActivator(inputChannel = "order-process-channel", outputChannel = "order-reply-channel", poller = @Poller(fixedDelay = "2000", maxMessagesPerPoll = "4"))
    public Message<Order> processOrder(Message<Order> order) {
        log.info("Order Processed, Placing Order ... {}", order.getPayload().getOrderId());
        order.getPayload().setOrderStatus("Order Successfully Placed");
        return order;
    }

    @ServiceActivator(inputChannel = "order-reply-channel", poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
    public void replyOrder(Message<Order> order)
    {
        log.info("Responding to client that -> Order Placed Successfully !!!  {} ", order.getPayload().getOrderId());
        MessageChannel replyChannel = (MessageChannel) order.getHeaders().getReplyChannel();
        replyChannel.send(order);
    }
}

/**
 * Logs for the request
 *
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-6] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='24', itemName='Garlic Naan', amount=60.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-1] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='20', itemName='Veg Biryani', amount=240.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-5] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='23', itemName='Palak Paneer', amount=200.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-3] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='15', itemName='Sweet Lassi', amount=90.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-4] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='22', itemName='Chicken Tikka', amount=180.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-2] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='21', itemName='Mutton Biryani', amount=260.0, orderStatus=''}
 * 2025-02-19T00:20:30.298+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-7] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='25', itemName='Mixed Veg Curry', amount=220.0, orderStatus=''}
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 24
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 23
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 15
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 20
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  24
 * 2025-02-19T00:20:30.299+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-6] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='26', itemName='Raita', amount=50.0, orderStatus=''}
 * 2025-02-19T00:20:31.304+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  23
 * 2025-02-19T00:20:31.305+05:30  INFO 26119 --- [Java_SpringIntegration] [onPool-worker-5] c.j.j.q.service.OrderService             : Order Received, Processing Order ... Order{orderId='27', itemName='Tandoori Chicken', amount=280.0, orderStatus=''}
 * 2025-02-19T00:20:32.304+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 22
 * 2025-02-19T00:20:32.304+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 21
 * 2025-02-19T00:20:32.304+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 25
 * 2025-02-19T00:20:32.304+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 26
 * 2025-02-19T00:20:32.305+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  15
 * 2025-02-19T00:20:33.310+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  20
 * 2025-02-19T00:20:34.306+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Order Processed, Placing Order ... 27
 * 2025-02-19T00:20:35.310+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  22
 * 2025-02-19T00:20:36.312+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  21
 * 2025-02-19T00:20:38.313+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  25
 * 2025-02-19T00:20:39.319+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  26
 * 2025-02-19T00:20:41.319+05:30  INFO 26119 --- [Java_SpringIntegration] [   scheduling-1] c.j.j.q.service.OrderService             : Responding to client that -> Order Placed Successfully !!!  27
 */