package com.java.pubsub.orderprocessing.config;


import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class KafkaInboundAdapter {

    private final KafkaConsumer<String, OrderProto.Order> kafkaConsumer;
    private final MessageChannel kafkaOutputChannel;

    public KafkaInboundAdapter(KafkaConsumer<String, OrderProto.Order> kafkaConsumer, MessageChannel kafkaOutputChannel) {
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaOutputChannel = kafkaOutputChannel;
    }

    public List<OrderProto.Order> pollOrders() {
        try {
            Thread.sleep(500); // Allow producer to send first
            ConsumerRecords<String, OrderProto.Order> records = kafkaConsumer.poll(Duration.ofMillis(1000));
            List<OrderProto.Order> orders = new ArrayList<>();

            records.forEach(record -> {
                orders.add(record.value());
                kafkaOutputChannel.send(new GenericMessage<>(record.value()));
            });
            if(!orders.isEmpty()) {
                log.info("Received {} orders from Kafka", orders.size());
                return orders;
            }
        }
        catch (org.apache.kafka.common.errors.WakeupException e) {
            // Handle consumer shutdown
            return Collections.emptyList();
        }
        catch (org.apache.kafka.common.KafkaException e) {
            // Handle Kafka-specific errors
            return Collections.emptyList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

