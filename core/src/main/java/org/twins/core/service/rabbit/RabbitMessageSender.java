package org.twins.core.service.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Service;

/**
 * Simple façade that hides ConnectionFactory retrieval and delegates message sending to {@link AmpqManager}.
 */
@Service
@RequiredArgsConstructor
public class RabbitMessageSender {

    private final RabbitConnectionService rabbitConnectionService;
    private final AmpqManager ampqManager;

    public void send(String url, String exchange, String routingKey, Object payload) {
        ConnectionFactory cf = rabbitConnectionService.getConnectionFactory(url);
        ampqManager.sendMessage(cf, exchange, routingKey, payload);
    }
}
