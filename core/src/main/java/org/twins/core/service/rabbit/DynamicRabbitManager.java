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

@Service
@Slf4j
@Lazy
public class DynamicRabbitManager implements DynamicAmpqManager {

    @Override
    public void createConnection(ConnectionFactory connectionFactory, String mainQueueName, String retryQueueName, String dlxName, String mainExc, Integer ttl) {
        RabbitAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
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

    @Override
    public void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.debug("Sent message {}", message);
    }
}
