package org.twins.core.featurer.notificator.context;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.history.HistoryEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Per-item {@link ContextCollector}: batch is implemented as a single {@link #beforeCollect(Collection)} hook
 * (bulk-load) followed by {@link #collectData} for each history. Concrete collectors extend this class and
 * implement {@link #collectData}; those that hit the DB override {@link #beforeCollect} to preload relations.
 * Collectors that produce i18n call {@link ContextCollectorBatch#addI18n} instead of translating in place.
 */
public abstract class ContextCollectorAtomic extends ContextCollector {

    @Override
    public final void collectDataBatch(ContextCollectorBatch batch, Properties properties) throws ServiceException {
        if (batch.isEmpty()) {
            return;
        }
        Map<HistoryEntity, Map<String, String>> contextByHistory = batch.getContextByHistory();
        beforeCollect(contextByHistory.keySet());
        for (Map.Entry<HistoryEntity, Map<String, String>> entry : contextByHistory.entrySet()) {
            collectData(entry.getKey(), entry.getValue(), properties);
        }
    }

    /**
     * Override to bulk-load relations needed by {@link #collectData} across the whole batch (default: no-op).
     */
    protected void beforeCollect(Collection<HistoryEntity> histories) throws ServiceException {
    }

    /**
     * Single-history convenience entry (kept for tests / non-batch callers).
     */
    public Map<String, String> collectData(HistoryEntity history, Map<String, String> context, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams);
        return collectData(history, context, properties);
    }

    protected abstract Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException;
}
