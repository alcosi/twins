package org.twins.core.service.rabbit;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public interface AmpqManager {

    void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message);
}
