package com.java.pubsub.orderprocessing.pubsub;

import com.java.pubsub.orderprocessing.protobuf.OrderProto.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

/**
 * OrderProducer is responsible for publishing {@link Order} messages to a Kafka topic.
 * <p>
 * This component wraps a {@link KafkaProducer} and provides a simple API to send Protobuf-based
 * order messages using the order ID as the message key.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Autowired
 * private OrderProducer orderProducer;
 *
 * Order order = Order.newBuilder()
 *     .setOrderId("order-123")
 *     .setItemName("Coffee")
 *     .setQuantity(2)
 *     .build();
 *
 * orderProducer.sendOrder("orders-topic", order);
 * }</pre>
 */
@Slf4j
@Component
public class OrderProducer {

    private final KafkaProducer<String, Order> kafkaOrderProducer;

    /**
     * Constructs an {@code OrderProducer} with the given KafkaProducer.
     *
     * @param producer Kafka producer configured for {@link Order} Protobuf messages
     */
    public OrderProducer(KafkaProducer<String, Order> producer) {
        this.kafkaOrderProducer = producer;
    }

    /**
     * Sends the given {@link Order} message to the specified Kafka topic.
     * <p>
     * The message key is derived from the {@code orderId} field of the order.
     *
     * @param topic the Kafka topic name to which the order should be sent
     * @param order the {@link Order} message to send
     */
    public void sendOrder(String topic, Order order) {
        log.info("Sending order: orderId={}, data={}", order.getOrderId(), order);
        ProducerRecord<String, Order> record = new ProducerRecord<>(topic, order.getOrderId(), order);
        kafkaOrderProducer.send(record);
    }

    /**
     * Closes the underlying Kafka producer.
     * <p>
     * Should be called during application shutdown to release Kafka resources gracefully.
     */
    public void close() {
        kafkaOrderProducer.close();
    }
}