package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;

import java.util.Properties;

/**
 * Per-item {@link Filler}: the batch is one {@link #beforeFill} bulk preload followed by
 * {@link #fill(Properties, FactoryItem, TwinEntity)} per factory item. A failing item of an optional
 * step is logged and skipped; a failing item of a mandatory step aborts the batch (fail-fast — the
 * end state is identical to the old per-item caller after transaction rollback). Mirrors
 * {@code RecipientResolverAtomic} / {@code NotifierAtomic}.
 */
@Slf4j
public abstract class FillerAtomic extends Filler {

    @Override
    public final void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        beforeFill(batch, templateTwin);
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                fill(properties, factoryItem, templateTwin);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }

    /**
     * Override to bulk-load relations needed by {@link #fill(Properties, FactoryItem, TwinEntity)}
     * across the whole batch — use the pre-derived views ({@link FactoryItemsBatch#getTwins()},
     * {@link FactoryItemsBatch#getTwinIds()}) instead of re-collecting them from the items.
     * Default: no-op.
     */
    protected void beforeFill(FactoryItemsBatch batch, TwinEntity templateTwin) throws ServiceException {
    }

    /**
     * Per-item fill. Public so concrete fillers stay directly unit-testable.
     */
    public abstract void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException;
}
