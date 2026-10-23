package org.twins.core.featurer.context;

import org.cambium.common.exception.ServiceException;

import java.util.Map;
import java.util.Properties;

/**
 * Per-item {@link ContextCollector}: batch is implemented as a single {@link #beforeCollect} hook
 * (bulk-load) followed by {@link #collectData} for each root. Concrete collectors extend this class and
 * implement {@link #collectData}; those that hit the DB override {@link #beforeCollect} to preload relations
 * (using the batch's lazy getters — {@link ContextCollectorBatch#getTwins()}, etc. — so they don't re-collect them).
 * Collectors that produce i18n call {@link ContextCollectorBatch#addI18n} instead of translating in place.
 */
public abstract class ContextCollectorAtomic<R extends ContextRoot> extends ContextCollector<R> {

    @Override
    public final void collectDataBatch(ContextCollectorBatch batch, Properties properties) throws ServiceException {
        if (batch.isEmpty()) {
            return;
        }
        Map<ContextRoot, Map<String, String>> contextByRoot = batch.getContextByRoot();
        beforeCollect(batch, properties);
        for (Map.Entry<ContextRoot, Map<String, String>> entry : contextByRoot.entrySet()) {
            collectData(castRoot(entry.getKey()), entry.getValue(), properties);
        }
    }

    /**
     * Always succeeds here (R erases to ContextRoot); the effective type check happens in the concrete
     * subclass bridge method — see {@link ContextCollector}.
     */
    @SuppressWarnings("unchecked")
    private R castRoot(ContextRoot root) {
        return (R) root;
    }

    /**
     * Override to bulk-load relations needed by {@link #collectData} across the whole batch (default: no-op).
     */
    protected void beforeCollect(ContextCollectorBatch batch, Properties properties) throws ServiceException {
    }

    protected abstract Map<String, String> collectData(R root, Map<String, String> context, Properties properties) throws ServiceException;
}
