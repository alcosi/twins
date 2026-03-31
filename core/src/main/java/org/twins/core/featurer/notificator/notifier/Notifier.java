package org.twins.core.featurer.notificator.notifier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@FeaturerType(id = FeaturerTwins.TYPE_48,
        name = "Notifier",
        description = "")
@Slf4j
public abstract class Notifier extends FeaturerTwins {
    @FeaturerParam(name = "Throw exception on null values", description = "", order = 1, defaultValue = "true")
    public static final FeaturerParamBoolean throwExceptionOnNullValues = new FeaturerParamBoolean("throwExceptionOnNullValues");

    protected final Map<String, Object> stubCache = new ConcurrentHashMap<>();

    public void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, HashMap<String, String> notifierParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, notifierParams);
        validateContext(context, throwExceptionOnNullValues.extract(properties));
        notify(recipientIds, context, eventCode, properties);
    }

    protected void validateContext(Map<String, String> context, boolean throwExceptionOnNullValues) throws ServiceException {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            if (entry.getValue() == null) {
                if (throwExceptionOnNullValues)
                    throw new ServiceException(ErrorCodeTwins.NOTIFICATION_CONTEXT_COLLECTOR_ERROR, "Entry in contect with key[" + entry.getKey() + "] has null value");
                context.remove(entry.getKey());
            }
        }
    }

    protected abstract void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException;
}
