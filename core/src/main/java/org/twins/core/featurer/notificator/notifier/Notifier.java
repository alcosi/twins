package org.twins.core.featurer.notificator.notifier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@FeaturerType(id = FeaturerTwins.TYPE_48,
        name = "Notifier",
        description = "")
@Slf4j
public abstract class Notifier extends FeaturerTwins {

    protected final Map<String, Object> stubCache = new ConcurrentHashMap<>();

    public void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, HashMap<String, String> notifierParams) throws ServiceException {
        validateContext(context);
        Properties properties = featurerService.extractProperties(this, notifierParams, new HashMap<>());
        notify(recipientIds, context, eventCode, properties);
    }

    protected void validateContext(Map<String, String> context) {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            if (entry.getValue() == null)
                context.remove(entry.getKey());
        }
    }

    protected abstract void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException;
}
