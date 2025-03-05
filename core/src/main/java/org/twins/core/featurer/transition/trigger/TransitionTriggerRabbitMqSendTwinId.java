package org.twins.core.featurer.transition.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1504,
        name = "ConnectEventTrigger",
        description = "Trigger for ...")
@RequiredArgsConstructor
public class TransitionTriggerRabbitMqSendTwinId extends TransitionTriggerRabbitMqConnection {

//    todo additional specific parameters for this trigger-rabbit-sender realisation
//    @FeaturerParam(name = "url", description = "rabbit server url", optional = false)
//    public static final FeaturerParamString url = new FeaturerParamString("url");

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) {
        log.debug("Sending to Rabbit");

        //todo form message body and send to queue

//        dynamicAmpqManager.sendMessage(MAIN_EXCHANGE.extract(properties), MAIN_QUEUE.extract(properties), twinEntity.getId());

    }
}
