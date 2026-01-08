package org.twins.core.service.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
public class RabbitManager implements AmpqManager {

    @Override
    public void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(new ObjectMapper()));
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, new MessagePostProcessorCustom());
        log.debug("Sent message {}", message);
    }
}
