package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_49,
        name = "Context collector",
        description = "")
@Slf4j
public abstract class ContextCollector extends FeaturerTwins {

    /**
     * Batch contract — collects context for a collector group sharing the same featurer params.
     * {@link ContextCollectorBatch#getContextByHistory()} is the shared per-history context accumulator
     * (history -> context map) owned by the caller; the histories to collect for are exactly its keySet.
     * i18n is registered via {@link ContextCollectorBatch#addI18n} and resolved by the caller afterwards.
     */
    public void collectDataBatch(ContextCollectorBatch batch, HashMap<String, String> recipientParams) throws ServiceException {
        if (batch.isEmpty()) {
            return;
        }
        Properties properties = featurerService.extractProperties(this, recipientParams);
        collectDataBatch(batch, properties);
    }

    public abstract void collectDataBatch(ContextCollectorBatch batch, Properties properties) throws ServiceException;
}
