package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.rabbit.AmpqManager;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1504,
        name = "RabbitMqSendTwin",
        description = "Trigger for sending event to rabbit")
@RequiredArgsConstructor
public class TransitionTriggerRabbitMqSendTwinIdAndDstStustId extends TransitionTriggerRabbitMqConnection {

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    private static final FeaturerParamString EXCHANGE = new FeaturerParamString("exchange");
    @FeaturerParam(name = "Queue", description = "Name of queue")
    private static final FeaturerParamString QUEUE = new FeaturerParamString("queue");
    private final AmpqManager ampqManager;

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) {
        log.debug("Sending to Rabbit");
        ConnectionFactory factory = TransitionTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TransitionTriggerRabbitMqConnection.URL.extract(properties));

        Map<String, UUID> eventMap = Map.of( "twinId" ,twinEntity.getId(), "statusId", dstTwinStatus.getId());
        ampqManager.sendMessage(factory, EXCHANGE.extract(properties), QUEUE.extract(properties), eventMap);
        log.debug("Done sending to Rabbit");
    }
}
