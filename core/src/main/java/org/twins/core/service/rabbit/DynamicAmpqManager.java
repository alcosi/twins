package org.twins.core.service.rabbit;

public interface DynamicAmpqManager {

    void sendMessage(String exchangeName, String routingKey, String message);
    void createConnection(String mainQueueName, String retryQueueName, String dlxName, String mainExc, Integer ttl);

}
