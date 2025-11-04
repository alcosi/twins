package org.twins.core.service.rabbit;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Lazy
public class RabbitManager implements AmpqManager {

    @Override
    public void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(new ObjectMapper()));

        // Enable mandatory flag so that unroutable messages are returned
        rabbitTemplate.setMandatory(true);

        // Confirm callback
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message confirmed by broker: {}", correlationData != null ? correlationData.getId() : "n/a");
            } else {
                log.warn("Message NOT confirmed by broker. Cause: {}", cause);
            }
        });

        // Returns callback for unroutable messages
        rabbitTemplate.setReturnsCallback(returned -> log.warn("Message returned: replyCode={}, replyText={}, exchange={}, routingKey={}",
                returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey()));

        // Correlation data for tracking confirms
        org.springframework.amqp.rabbit.connection.CorrelationData correlationData = new org.springframework.amqp.rabbit.connection.CorrelationData(java.util.UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, new MessagePostProcessorCustom(), correlationData);
        log.debug("Sent message {} with correlationId {}", message, correlationData.getId());
    }
}
