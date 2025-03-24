### **README for Kafka-based Order Processing System**

This implementation sets up a Kafka-based system for processing orders using Protobuf serialization. 

It includes Kafka producers and consumers, Spring Integration channels, and message handlers for durable and non-durable order processing.

---

### **Design Diagram**

![C4 Design Diagram](./design_c4.png)
![Design Diagram](./design.png)

---

### **Process Overview**
1. **Order Production**:
    - Orders are serialized using Protobuf and sent to a Kafka topic (`orders-topic`) via a `KafkaProducer`.
    - The producer uses Protobuf serialization for message values and String serialization for keys.

2. **Order Consumption**:
    - Two types of consumers are configured:
        - A **durable consumer** that ensures messages are stored in a cache to avoid duplication.
        - A **non-durable consumer** that processes transient messages without persistence.
    - Both consumers use Protobuf deserialization to interpret the messages.

3. **Spring Integration**:
    - Kafka messages are polled by an inbound adapter and sent to an output channel (`kafkaOutputChannel`).
    - The messages are routed to specific input channels (`durableInputChannel` or `nonDurableInputChannel`) based on the type of subscription.
    - Service activators process the messages using respective message handlers.

4. **Durability**:
    - Durable orders are cached locally using an `AbstractCache` implementation to prevent duplicate processing.

5. **Non-Durable Orders**:
    - Non-durable orders are processed immediately without caching, suitable for transient use cases.

---

### **Benefits of This Design**
1. **Scalability**: Kafka ensures high throughput for message production and consumption.
2. **Resilience**: Durable subscribers prevent data loss by caching processed messages.
3. **Flexibility**: Non-durable subscribers allow lightweight, transient message processing.
4. **Extensibility**: Modular design enables easy addition of new features or subscribers.
5. **Serialization Efficiency**: Protobuf ensures compact, efficient data transfer over Kafka.

This setup is ideal for systems requiring reliable, scalable, and efficient order processing with both durable and non-durable workflows.
