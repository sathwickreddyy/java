package com.java.pubsub.orderprocessing.config;


import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class KafkaInboundAdapter {

    private final KafkaConsumer<String, OrderProto.Order> kafkaConsumer;
    @Qualifier("defaultKafkaChannel")
    private final MessageChannel kafkaOutputChannel;

    public KafkaInboundAdapter(KafkaConsumer<String, OrderProto.Order> kafkaConsumer, MessageChannel kafkaOutputChannel) {
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaOutputChannel = kafkaOutputChannel;
    }

    @InboundChannelAdapter(value = "kafkaOutputChannel", poller = @Poller(fixedDelay = "5000"))
    public void pollOrders() {
        ConsumerRecords<String, OrderProto.Order> records = kafkaConsumer.poll(Duration.ofMillis(1000));
        records.forEach(record -> kafkaOutputChannel.send(new GenericMessage<>(record.value())));
    }
}
