package org.twins.core.service.rabbit;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public interface DynamicAmpqManager {

    void sendMessage(ConnectionFactory connectionFactory, String exchangeName, String routingKey, Object message);
    void createConnection(ConnectionFactory connectionFactory,String mainQueueName, String retryQueueName, String dlxName, String mainExc, Integer ttl);

}
