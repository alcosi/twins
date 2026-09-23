package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class FieldLookuperNearest extends FieldLookuper {
    // twinClassFieldService is inherited (protected) from FieldLookuper — do not re-declare it here

    public Map<FactoryItem, FieldValue> lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID lookupTwinClassFieldId) throws ServiceException {
        var ret =  new HashMap<FactoryItem, FieldValue>(factoryItemsBatch.size());
        var twinClassField = twinClassFieldService.findEntitySafe(lookupTwinClassFieldId);
        if (!factoryItemsBatch.getTwins().isEmpty())
            beforeLookup(factoryItemsBatch, twinClassField);
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            ret.put(factoryItem, lookupFieldValue(factoryItem, twinClassField));
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

    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException;
}
