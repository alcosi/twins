package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByField extends FieldLookuper implements FieldLookuperLinked {
    @Autowired
    protected TwinLinkService twinLinkService;

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        var ret = LookupResult.empty(factoryItemsBatch.size());
        beforeLookup(factoryItemsBatch);
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            try {
                var value = lookupFieldValue(factoryItem, linkedTwinByTwinClassFieldId, lookupTwinClassFieldId);
                if (value == null) {
                    value = twinService.createFieldValue(twinClassFieldService.findEntitySafe(lookupTwinClassFieldId)); //create field as undefined
                }
                ret.values().put(factoryItem, value);
            } catch (ServiceException ex) {
                ret.failures().put(factoryItem, ex); // per-item isolation — the caller re-throws per item
            }
        }
        return ret;
    }

    /**
     * Override to bulk-load relations needed by the per-item lookup across the whole batch — use the
     * pre-derived views ({@link FactoryItemsBatch#getTwins()}, {@link FactoryItemsBatch#getContextTwins()})
     * instead of re-collecting them from the items. Default: no-op.
     */
    protected void beforeLookup(FactoryItemsBatch batch) throws ServiceException {
    }

    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException;
}
