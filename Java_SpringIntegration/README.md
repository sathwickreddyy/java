# Java Spring Integration - Reference Guide

## Overview
- Spring Integration is a powerful framework that extends the Spring programming model to support integration patterns commonly used in enterprise application integration (EAI). It provides a lightweight messaging-based approach to integrate applications and systems efficiently.
- Spring Integration is helpful when we want to integrate our application with external/3rd party systems or services.

## Key Features
- **Message-Based Communication**: Enables asynchronous message-driven architecture.
- **Enterprise Integration Patterns (EIP)**: Supports widely used patterns such as Channel, Transformer, Splitter, Aggregator, and Router.
- **Declarative Configuration**: Supports Java-based and XML configuration.
- **Adapters & Connectors**: Provides pre-built adapters for messaging systems, databases, HTTP, FTP, JMS, Kafka, and more.
- **Event-Driven Architecture**: Facilitates event-driven microservices.
- **Routing and Filtering**: Enables conditional message processing and routing.
- **Monitoring & Management**: Supports Actuator endpoints and metrics monitoring.

## Installation
To use Spring Integration in a Spring Boot project, add the necessary dependencies in `build.gradle` (for Gradle).

### Gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-integration'
    implementation 'org.springframework.integration:spring-integration-core'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

## Core Concepts

### 1. **Message**
A message is a payload wrapped with metadata (headers). It follows the format:
```java
Message<String> message = MessageBuilder.withPayload("Hello World")
    .setHeader("headerKey", "headerValue")
    .build();
```

### 2. **Channels and Their Usage**
A `MessageChannel` is a conduit through which messages flow between components.
- **DirectChannel**: Delivers messages synchronously to a single subscriber.
- **QueueChannel**: Buffers messages for asynchronous delivery.
- **PublishSubscribeChannel**: Delivers messages to multiple subscribers.

Example:
```java
@Bean
public MessageChannel directChannel() {
    return new DirectChannel();
}
```

### 3. **Endpoints**
Endpoints process messages and perform actions like transformation, filtering, or routing.

- **Transformer**: Converts message format.
```java
@Transformer(inputChannel = "inputChannel", outputChannel = "outputChannel")
public String transform(String message) {
    return message.toUpperCase();
}
```

- **Router**: Routes messages based on conditions.
```java
@Router(inputChannel = "routerChannel")
public String route(String message) {
    return message.contains("error") ? "errorChannel" : "outputChannel";
}
```

- **Service Activator**: Invokes a service method when a message arrives.
```java
@ServiceActivator(inputChannel = "inputChannel")
public void processMessage(String message) {
    System.out.println("Received: " + message);
}
```

## Transformers and Their Usage
Transformers are used to modify or convert message payloads before passing them to the next component.

Example:
```java
@Transformer(inputChannel = "inputChannel", outputChannel = "transformedChannel")
public String transformMessage(String message) {
    return "Transformed: " + message;
}
```

## Example - File Integration
A simple example of reading files and processing them:

### File Polling Configuration
```java
@Bean
public IntegrationFlow fileReadingFlow() {
    return IntegrationFlows.from(Files.inboundAdapter(new File("/input"))
            .patternFilter("*.txt"),
            e -> e.poller(Pollers.fixedRate(5000)))
        .transform(Files.toStringTransformer())
        .channel("fileProcessingChannel")
        .get();
}
```

### File Processing Service
```java
@ServiceActivator(inputChannel = "fileProcessingChannel")
public void processFileContent(String content) {
    System.out.println("Processing file content: " + content);
}
```

## Monitoring & Actuator Integration
Spring Integration supports monitoring via Spring Boot Actuator:

### Gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```
Enable metrics in `application.properties`:
```properties
management.endpoints.web.exposure.include=integrationgraph, metrics, health
```
Access the metrics via:
```
http://localhost:8080/actuator/integrationgraph
```

## Conclusion
Spring Integration is a robust and flexible framework for enterprise integration, supporting a variety of messaging and event-driven architectures. By leveraging Spring Integration, applications can seamlessly connect disparate systems, process messages efficiently, and scale effectively.

For more details, refer to the [official documentation](https://docs.spring.io/spring-integration/reference/html/).

