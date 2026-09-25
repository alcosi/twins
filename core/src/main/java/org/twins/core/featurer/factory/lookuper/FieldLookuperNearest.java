package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

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
                var value = lookupFieldValue(factoryItem, twinClassField);
                ret.values().put(factoryItem, value);
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

    /**
     * Override to bulk-load relations needed by the per-item lookup across the whole batch — use the
     * pre-derived views ({@link FactoryItemsBatch#getTwins()}, {@link FactoryItemsBatch#getContextTwins()})
     * instead of re-collecting them from the items. Default: no-op.
     */
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
    }

    public FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        var value = lookupFieldValueOrNull(factoryItem, lookupTwinClassField);
        if (value == null) {
            value = twinService.createFieldValue(lookupTwinClassField); //create field as undefined
        }
        return value;
    }

    public abstract FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException;
}
