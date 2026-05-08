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
        var nullKeys = new ArrayList<String>();
        for (var entry : context.entrySet()) {
            if (entry.getValue() == null) {
                nullKeys.add(entry.getKey());
            }
        }

        for (var key : nullKeys) {
            if (throwExceptionOnNullValues) {
                throw new ServiceException(ErrorCodeTwins.NOTIFICATION_CONTEXT_COLLECTOR_ERROR, "Entry in contect with key[" + key + "] has null value");
            }

            context.remove(key);
        }
    }

    protected abstract void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException;
}
