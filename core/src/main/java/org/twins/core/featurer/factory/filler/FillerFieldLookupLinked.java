package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.factory.lookuper.FieldLookuperLinked;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;
import java.util.UUID;

/**
 * Per-item {@link Filler} driven by one batch field lookup through a linked twin: the
 * {@link FieldLookuperLinked} batch entry runs once per step (bulk preloads once, per-item failures
 * isolated into {@link LookupResult#failures()}), then
 * {@link #fill(Properties, FactoryItem, TwinEntity, FieldValue)} is invoked per factory item with
 * the pre-resolved value. The item's own lookup failure is re-thrown inside the loop —
 * optional-step semantics identical to the old per-item caller.
 */
@Slf4j
public abstract class FillerFieldLookupLinked extends Filler {

    @Override
    public final void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        LookupResult result = lookuper().lookupFieldValue(batch, linkedById(properties), lookupFieldId(properties));
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem);
                fill(properties, factoryItem, templateTwin, result.value(factoryItem));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }

    /** Linked lookuper of this filler — one of the FieldLookupers linked-twin beans. */
    protected abstract FieldLookuperLinked lookuper();

    /** Id of the link (or link field) the source twin is found by, extracted once per batch. */
    protected abstract UUID linkedById(Properties properties) throws ServiceException;

    /** Field id to look up on the linked twin, extracted once per batch. */
    protected abstract UUID lookupFieldId(Properties properties) throws ServiceException;

    /** Per-item fill with the pre-resolved lookup value. */
    public abstract void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException;
}
