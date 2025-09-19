package org.twins.core.featurer.dispatcher;

import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;

@FeaturerType(id = FeaturerTwins.TYPE_44,
        name = "Dispatcher",
        description = "Dispatching notifications on various events")

public abstract class Dispatcher extends FeaturerTwins {


    public abstract void sendMessage(HashMap<String, String> subscriberParams, Object message);

}
