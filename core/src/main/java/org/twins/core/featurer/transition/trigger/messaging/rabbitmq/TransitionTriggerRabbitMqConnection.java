package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;

@Slf4j
public abstract class TransitionTriggerRabbitMqConnection extends TransitionTrigger {
    @FeaturerParam(name = "url", description = "rabbit server url")
    public static final FeaturerParamString url = new FeaturerParamString("url");
}
