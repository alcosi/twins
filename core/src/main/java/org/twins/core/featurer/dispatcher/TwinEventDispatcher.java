package org.twins.core.featurer.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.rabbit.RabbitMessageSender;

import java.util.HashMap;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4201,
        name = "TwinEventDispatcher",
        description = "Dispatching twin updates to RabbitMQ"
)
@Slf4j
@RequiredArgsConstructor
public class TwinEventDispatcher extends Dispatcher {

    private final RabbitMessageSender rabbitMessageSender;

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString exchange = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString queue = new FeaturerParamString("queue");

    @FeaturerParam(name = "url", description = "rabbit server url")
    public static final FeaturerParamString url = new FeaturerParamString("url");

    public void sendMessage(HashMap<String, String> subscriberParams, Object message) {
        log.info("Sending message: {}", message);
        try {
            Properties properties = featurerService.extractProperties(this, subscriberParams, new HashMap<>());
            rabbitMessageSender.send(
                    url.extract(properties),
                    exchange.extract(properties),
                    queue.extract(properties),
                    message
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
