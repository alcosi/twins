package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

/**
 * The two-hook read contract of the nearest family — see ai/plans/lookuper-navigation-stages.md.
 * A concrete lookuper is ONE obvious method ({@link #lookupFieldValueOrNull}: the whole per-item
 * logic) plus an optional {@link #beforeLookup} bulk for the batch. The template around them
 * provides the shared contract: per-item isolation (a failure fails only its item and lands in
 * {@link LookupResult#failures()}) and the not-found conversion (a null value becomes an UNDEFINED
 * value). True navigation (start -> hops -> read) lives in {@link FieldLookuperNavigated} — the
 * linked family only.
 */
public abstract class FieldLookuperNearest extends FieldLookuper {

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID lookupTwinClassFieldId) throws ServiceException {
        return lookupFieldValue(factoryItemsBatch, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId));
    }

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, TwinClassFieldEntity twinClassField) throws ServiceException {
        var ret = LookupResult.empty(factoryItemsBatch.size());
        if (!factoryItemsBatch.getTwins().isEmpty())
            beforeLookup(factoryItemsBatch, twinClassField);
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            try {
                ret.values().put(factoryItem, lookupFieldValue(factoryItem, twinClassField));
            } catch (ServiceException ex) {
                ret.failures().put(factoryItem, ex); // per-item isolation — the caller re-throws per item
            }
        }
        return ret;
    }

    /**
     * UUID-based convenience for per-item callers that did not resolve the field entity yet —
     * the entity is resolved per call (cached service lookup); batch callers use the batch entry,
     * which resolves it once per batch.
     */
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        return lookupFieldValue(factoryItem, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId));
    }

    public FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        var value = lookupFieldValueOrNull(factoryItem, lookupTwinClassField);
        if (value == null) {
            value = twinService.createFieldValue(lookupTwinClassField); //create field as undefined
        }
        return value;
    }

    /**
     * Optional bulk for the whole batch right before the per-item loop — preload what the per-item
     * reads lazily need (e.g. {@code loadTwinFields(batch.getContextTwins(), field)}). Default: no-op.
     */
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
    }

    /** The whole per-item logic of the lookuper; null = not found (becomes an undefined value). */
    public abstract FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException;
}
