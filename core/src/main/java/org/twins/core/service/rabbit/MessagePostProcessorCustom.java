package org.twins.core.service.rabbit;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class MessagePostProcessorCustom implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().getHeaders().remove("__TypeId__");
        return message;
    }

    @Override
    public Message postProcessMessage(Message message, Correlation correlation) {
        message.getMessageProperties().getHeaders().remove("__TypeId__");
        return MessagePostProcessor.super.postProcessMessage(message, correlation);
    }

    @Override
    public Message postProcessMessage(Message message, Correlation correlation, String exchange, String routingKey) {
        message.getMessageProperties().getHeaders().remove("__TypeId__");
        return MessagePostProcessor.super.postProcessMessage(message, correlation, exchange, routingKey);
    }

}
