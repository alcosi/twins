package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.history.HistoryEntity;

import java.util.*;

/**
 * Per-item {@link RecipientResolver}: batch is implemented as a single {@link #beforeResolve(Collection)} hook
 * (bulk-load) followed by {@link #resolve} for each history. Concrete resolvers extend this class and implement
 * {@link #resolve}; those that hit the DB override {@link #beforeResolve} to preload relations for the whole batch.
 */
public abstract class RecipientResolverAtomic extends RecipientResolver {

    @Override
    public final void resolveBatch(RecipientResolveBatch context, Properties properties) throws ServiceException {
        if (context.isEmpty()) {
            return;
        }
        Map<HistoryEntity, Set<UUID>> recipientIdsByHistory = context.getRecipientIdsByHistory();
        beforeResolve(recipientIdsByHistory.keySet());
        for (Map.Entry<HistoryEntity, Set<UUID>> entry : recipientIdsByHistory.entrySet()) {
            resolve(entry.getKey(), entry.getValue(), properties);
        }
    }

    /**
     * Override to bulk-load relations needed by {@link #resolve} across the whole batch (default: no-op).
     */
    protected void beforeResolve(Collection<HistoryEntity> histories) throws ServiceException {
    }

    /**
     * Single-history convenience entry (kept for tests / non-batch callers).
     */
    public void resolve(HistoryEntity history, Set<UUID> recipientIds, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams);
        resolve(history, recipientIds, properties);
    }

    protected abstract void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException;
}
