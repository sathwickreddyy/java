package com.java.pubsub.orderprocessing.config;

import com.java.pubsub.orderprocessing.protobuf.OrderProto;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Collections;
import java.util.Properties;

/**
 * Configuration class for setting up Kafka producers and consumers using Protobuf serialization.
 * <p>
 * This class sets up:
 * <ul>
 *     <li>Producer properties with {@link KafkaProtobufSerializer} for serializing Protobuf messages</li>
 *     <li>Consumer properties with {@link KafkaProtobufDeserializer} for deserializing Protobuf messages</li>
 *     <li>A {@link KafkaProducer} bean for producing {@link OrderProto.Order} messages</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Autowire the KafkaProducer bean in your service
 * @Autowired
 * private KafkaProducer<String, OrderProto.Order> producer;
 *
 * // Send a message
 * ProducerRecord<String, OrderProto.Order> record =
 *      new ProducerRecord<>("orders", "order-key", orderProtoMessage);
 * producer.send(record);
 * }</pre>
 */
@Configuration
@ImportResource("classpath:/integration/pubsub-integration.xml")
public class KafkaConfig {

    /**
     * Builds Kafka producer properties using Protobuf serialization for values.
     *
     * <ul>
     *     <li>Sets the bootstrap server to localhost:9092</li>
     *     <li>Uses {@link StringSerializer} for keys</li>
     *     <li>Uses {@link KafkaProtobufSerializer} for Protobuf values</li>
     * </ul>
     *
     * @return Kafka producer {@link Properties} object
     */
    private Properties producerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        return props;
    }

    /**
     * Builds Kafka consumer properties using Protobuf deserialization for values.
     *
     * <ul>
     *     <li>Sets the bootstrap server to localhost:9092</li>
     *     <li>Uses {@link StringDeserializer} for keys</li>
     *     <li>Uses {@link KafkaProtobufDeserializer} for Protobuf values</li>
     *     <li>Reads from the earliest offset</li>
     * </ul>
     *
     * @param groupId Kafka consumer group ID
     * @return Kafka consumer {@link Properties} object
     */
    public static Properties consumerProperties(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put("specific.protobuf.value.type", OrderProto.Order.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        return props;
    }

    /**
     * Spring bean that provides a Kafka producer for publishing {@link OrderProto.Order} messages.
     *
     * @return a {@link KafkaProducer} configured for sending Protobuf messages
     */
    @Bean
    public KafkaProducer<String, OrderProto.Order> kafkaOrderProducer() {
        return new KafkaProducer<>(producerProperties());
    }

    /**
     * Kafka consumer for durable order subscription
     *  <pre>
     *  🧩 orders-group → This is the Consumer Group ID used to track offsets of consumed messages.
     * 	•	Used to track offsets of consumed messages.
     * 	•	Helps Kafka coordinate load balancing and parallel processing among multiple consumers.
     * 	•	Multiple consumers with the same group ID will share the work (consume different partitions).
     * 	</pre>
     *
     *  <pre>
     *  📨 orders-topic → This is the Kafka Topic Name
     * 	•	The name of the topic your app subscribes to in order to receive messages.
     *  </pre>
     */
    @Bean(name = "kafkaConsumer")
    public KafkaConsumer<String, OrderProto.Order> kafkaConsumer() {
        try {
            KafkaConsumer<String, OrderProto.Order> consumer =
                    new KafkaConsumer<>(consumerProperties("orders-group"));
            consumer.subscribe(Collections.singleton("orders-topic"));
            return consumer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Kafka consumer: " + e.getMessage(), e);
        }
    }
}