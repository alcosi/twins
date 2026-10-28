package org.twins.core.featurer.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

/**
 * @param <R> the root kind this collector reads from — {@link ContextRoot} for twin-driven collectors
 *            (usable with any root), {@link ContextRootHistory} for history-scoped ones. Feeding a root
 *            of the wrong kind (e.g. a plain twin root to a history collector) fails with a CCE in the
 *            subclass bridge method — that is a context-configuration error, not data to skip.
 */
@FeaturerType(id = FeaturerTwins.TYPE_49,
        name = "Context collector",
        description = "")
@Slf4j
public abstract class ContextCollector<R extends ContextRoot> extends FeaturerTwins {

    /**
     * Batch contract — collects context for a collector group sharing the same featurer params.
     * {@link ContextCollectorBatch#getContextByRoot()} is the shared per-root context accumulator
     * (root -> context map) owned by the caller; the roots to collect for are exactly its keySet.
     * i18n is registered via {@link ContextCollectorBatch#addI18n} and resolved by the caller afterwards.
     */
    public void collectDataBatch(ContextCollectorBatch batch, HashMap<String, String> collectorParams) throws ServiceException {
        if (batch.isEmpty()) {
            return;
        }
        Properties properties = featurerService.extractProperties(this, collectorParams);
        collectDataBatch(batch, properties);
    }

    public abstract void collectDataBatch(ContextCollectorBatch batch, Properties properties) throws ServiceException;
}
