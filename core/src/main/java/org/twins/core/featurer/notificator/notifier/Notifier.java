package org.twins.core.featurer.notificator.notifier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@FeaturerType(id = FeaturerTwins.TYPE_48,
        name = "Notifier",
        description = "")
@Slf4j
public abstract class Notifier extends FeaturerTwins {
    @FeaturerParam(name = "Throw exception on null values", description = "", order = 1, defaultValue = "true")
    public static final FeaturerParamBoolean throwExceptionOnNullValues = new FeaturerParamBoolean("throwExceptionOnNullValues");

    protected final Map<String, Object> stubCache = new ConcurrentHashMap<>();

    public Set<NotifyEvent> notify(HashMap<String, String> notifierParams, Set<NotifyEvent> notifyEvents) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, notifierParams);
        boolean throwOnNull = throwExceptionOnNullValues.extract(properties);
        for (NotifyEvent notifyEvent : notifyEvents) {
            validateContext(notifyEvent.context(), throwOnNull);
        }
        return notify(properties, notifyEvents);
    }

    protected void validateContext(Map<String, String> context, boolean throwExceptionOnNullValues) throws ServiceException {
        var it = context.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getValue() == null) {
                if (throwExceptionOnNullValues) {
                    throw new ServiceException(ErrorCodeTwins.NOTIFICATION_CONTEXT_COLLECTOR_ERROR, "Entry with key " + entry.getKey() + " has null value");
                }
                it.remove();
            }
        }
    }

    /**
     * Sends the whole event batch of one notifier channel (all events of one chunk that share this
     * channel). Implementations either loop per event ({@link NotifierAtomic}) or send natively in
     * batch. MUST NOT throw for individual send failures — return the failed events instead, so the
     * caller can attribute failures per task; throw only for channel-level errors (configuration,
     * connectivity) that fail the whole batch.
     *
     * @return the events that failed to send (empty set = all sent)
     */
    protected abstract Set<NotifyEvent> notify(Properties properties, Set<NotifyEvent> notifyEvents) throws ServiceException;
}
