package org.twins.core.featurer.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.exception.ErrorCodeFeaturer;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.rabbit.RabbitService;
import org.twins.core.service.rabbit.RabbitMessageSender;

import java.util.HashMap;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4701,
        name = "TwinEventDispatcher",
        description = "Dispatching twin updates to RabbitMQ"
)
@Slf4j
@RequiredArgsConstructor
public class TwinEventDispatcher extends Dispatcher {

    private final RabbitMessageSender rabbitMessageSender;
    private final RabbitService rabbitService;

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString exchangeNameParam = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString queueNameParam = new FeaturerParamString("queue");

    @FeaturerParam(name = "url", description = "rabbit server url")
    public static final FeaturerParamString urlParam = new FeaturerParamString("url");

    public void sendMessage(HashMap<String, String> subscriberParams, Object message) {
        try {
            Properties properties = featurerService.extractProperties(this, subscriberParams, new HashMap<>());

            String exchangeName = exchangeNameParam.extract(properties);
            String queueName = queueNameParam.extract(properties);
            String url = urlParam.extract(properties);

            if (rabbitService.isDeclared(url, exchangeName, queueName)) {
                log.info("Sending message to exchange='{}', queue='{}', url='{}': {}", exchangeName, queueName, url, message);

                rabbitMessageSender.send(
                        urlParam.extract(properties),
                        exchangeName,
                        queueNameParam.extract(properties),
                        message
                );
            } else {
                throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, "Unable to declare rabbit objects with url[" + url + "], queue[" + queueName + "], exchange[" + exchangeName + "]");
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }
}
