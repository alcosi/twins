package org.twins.core.service.rabbit;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A service implementation of {@link DynamicAmpqManager} responsible for managing
 * RabbitMQ resources dynamically. This class provides functionality to create
 * RabbitMQ queues, exchanges, and bindings, as well as send messages to
 * RabbitMQ exchanges using specified routing keys.
 * This manager is useful for dynamic runtime management of RabbitMQ messaging
 * infrastructure and simplifies the process of interacting with RabbitMQ through
 * {@link RabbitTemplate} and administrative operations via {@link RabbitAdmin}.
 * Intended to be used as a Spring-managed service with dependency injection.
 */
@Service
@Slf4j
@Lazy
public class DynamicRabbitManager implements DynamicAmpqManager {

    /**
     * A thread-safe instance of {@link RabbitTemplate} used to interact with RabbitMQ.
     * Provides convenience methods to send messages to exchanges, with customizable
     * routing keys, facilitating structured message publishing in the messaging system.
     * Configured and managed by the containing {@link DynamicRabbitManager} class to
     * handle AMQP operations such as message publishing.
     */
    private final RabbitTemplate rabbitTemplate;
    /**
     * Manages administrative operations for AMQP, including declaring queues,
     * exchanges, and bindings as part of the RabbitMQ management.
     * <p>
     * This instance of RabbitAdmin interacts with the RabbitMQ broker, facilitating
     * tasks such as the creation of messaging infrastructure including queues,
     * exchanges, and bindings dynamically at runtime.
     * </p>
     * <p>
     * Used internally to support the implementation details of the {@link DynamicRabbitManager}.
     * </p>
     */
    private final RabbitAdmin amqpAdmin;

    /**
     * Constructs a new instance of DynamicRabbitManager.
     * This class is responsible for managing RabbitMQ resources dynamically, including queues, exchanges, bindings,
     * and sending messages through RabbitTemplate and RabbitAdmin.
     *
     * @param connectionFactory the ConnectionFactory used to create RabbitMQ connections.
     */
    public DynamicRabbitManager(ConnectionFactory connectionFactory) {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.amqpAdmin = new RabbitAdmin(connectionFactory);
    }

    /**
     * Creates a connection to a queue with specified parameters and sets up
     * a retry mechanism using a Dead Letter Exchange (DLX).
     *
     * @param mainQueueName Name of the main queue.
     * @param retryQueueName Name of the retry queue.
     * @param dlxName Name of the Dead Letter Exchange (DLX).
     * @param mainExc Name of the Main Exchange (DLX).
     * @param ttl Time-to-live (TTL) for messages in the retry queue, in milliseconds.
     */
    @Override
    public void createConnection(String mainQueueName, String retryQueueName, String dlxName, String mainExc, Integer ttl) {



        DirectExchange mainExchange = new DirectExchange(mainExc, true, false);
        amqpAdmin.declareExchange(mainExchange);

        Map<String, Object> mainQueueArgs = new HashMap<>();
        mainQueueArgs.put("x-dead-letter-exchange", dlxName);
        mainQueueArgs.put("x-dead-letter-routing-key", retryQueueName);
        Queue mainQueue = new Queue(mainQueueName, true, false, false, mainQueueArgs);
        amqpAdmin.declareQueue(mainQueue);
        log.debug("Create connection to queue {}: {}", mainExchange, mainQueueName);

        if (StringUtils.isNotBlank(retryQueueName) && StringUtils.isNotBlank(dlxName)) {

            DirectExchange dlxExchange = new DirectExchange(dlxName, true, false);
            amqpAdmin.declareExchange(dlxExchange);

            Map<String, Object> retryQueueArgs = new HashMap<>();
            retryQueueArgs.put("x-message-ttl", ttl == null ? 10000 : ttl);
            retryQueueArgs.put("x-dead-letter-exchange", dlxName);
            retryQueueArgs.put("x-dead-letter-routing-key", mainQueueName);
            Queue retryQueue = new Queue(retryQueueName, true, false, false, retryQueueArgs);
            amqpAdmin.declareQueue(retryQueue);
           logCreating(dlxName, retryQueueName);

            Binding dlxBinding = BindingBuilder.bind(retryQueue)
                    .to(dlxExchange)
                    .with(retryQueueName);
            amqpAdmin.declareBinding(dlxBinding);
            logCreating(dlxExchange.getName(), retryQueueName);

            Binding dlxMainBinding = BindingBuilder.bind(mainQueue)
                    .to(dlxExchange)
                    .with(mainQueueName);
            amqpAdmin.declareBinding(dlxMainBinding);
        }

        Binding mainBinding = BindingBuilder.bind(mainQueue)
                .to(mainExchange)
                .with(mainQueueName);
        amqpAdmin.declareBinding(mainBinding);
      logCreating(mainExchange.getName(), mainQueueName);
    }

    private void logCreating(String exchange, String queue) {
        log.debug("Create  binding for Exchange {} and Queue {}", exchange, queue);
    }

    /**
     * Sends a message to the specified exchange with the given routing key.
     *
     * @param exchangeName the name of the exchange to which the message will be sent
     * @param routingKey the routing key to use for delivering the message
     * @param message the message content to be sent
     */
    @Override
    public void sendMessage(String exchangeName, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.debug("Sent message {}", message);
    }
}
