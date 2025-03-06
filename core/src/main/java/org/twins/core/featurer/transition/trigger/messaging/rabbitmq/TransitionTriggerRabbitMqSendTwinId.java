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
import org.twins.core.service.rabbit.DynamicAmpqManager;

import java.util.Properties;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1504,
        name = "ConnectEventTrigger",
        description = "Trigger for ...")
@RequiredArgsConstructor
public class TransitionTriggerRabbitMqSendTwinId extends TransitionTriggerRabbitMqConnection {

    @FeaturerParam(name = "Main exchange", description = "Name of main exchange", order = 1)
    private static final FeaturerParamString MAIN_EXCHANGE = new FeaturerParamString("mainExc");
    @FeaturerParam(name = "Main queue", description = "Name of main queue", order = 2) //todo order?
    private static final FeaturerParamString MAIN_QUEUE = new FeaturerParamString("mainQueue");
    private final DynamicAmpqManager dynamicAmpqManager;

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) {

        ConnectionFactory factory = TransitionTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TransitionTriggerRabbitMqConnection.URL.extract(properties));

        dynamicAmpqManager.sendMessage(factory, MAIN_EXCHANGE.extract(properties), MAIN_QUEUE.extract(properties), twinEntity);
        log.debug("Sending to Rabbit");
    }
}
