package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwin;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;

import java.util.Properties;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1504,
        name = "RabbitMqSendTwin",
        description = "Trigger for sending event to rabbit")
@RequiredArgsConstructor
public class TwinTriggerRabbitMqSendTwin extends TwinTriggerRabbitMqConnection {

    private final AmpqManager ampqManager;

    private final AuthService authService;

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString exchange = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString queue = new FeaturerParamString("queue");

    @FeaturerParam(name = "Operation", description = "Name of operation")
    public static final FeaturerParamString operation = new FeaturerParamString("operation");


    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();

        log.debug("Sending to Rabbit");
        ConnectionFactory factory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TwinTriggerRabbitMqConnection.url.extract(properties));

        RabbitMqMessagePayloadTwin payload = new RabbitMqMessagePayloadTwin(
                twinEntity.getId(),
                apiUser.getUserId(),
                apiUser.getDomainId(),
                apiUser.getBusinessAccountId(),
                operation.extract(properties)
        );
        ampqManager.sendMessage(factory, exchange.extract(properties), queue.extract(properties), payload);
        log.debug("Done sending to Rabbit");
    }
}
