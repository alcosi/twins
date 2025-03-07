package org.twins.core.service.rabbit;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Lazy
public class RabbitManager implements AmpqManager {

    @Override
    public void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.debug("Sent message {}", message);
    }
}
