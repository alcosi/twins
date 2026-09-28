package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByLink extends FieldLookuper implements FieldLookuperLinked {
    @Autowired
    TwinLinkService twinLinkService;

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
        return lookupFieldValue(factoryItemsBatch, linkedTwinByLinkId, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId)); // entity resolved once per batch
    }

    /**
     * Batch lookup with per-item isolation. A null per-item value becomes an undefined
     * {@link FieldValue} — the not-found contract of the FieldLookuperNearest family.
     */
    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByLinkId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        var ret = LookupResult.empty(factoryItemsBatch.size());
        beforeLookup(factoryItemsBatch, linkedTwinByLinkId, lookupTwinClassField);
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            try {
                var value = lookupFieldValueOrNull(factoryItem, linkedTwinByLinkId, lookupTwinClassField);
                ret.values().put(factoryItem, value == null ? twinService.createFieldValue(lookupTwinClassField) : value); //create field as undefined
            } catch (ServiceException ex) {
                ret.failures().put(factoryItem, ex); // per-item isolation — the caller re-throws per item
            }
        }
        return ret;
    }

    /**
     * Override to bulk-load relations needed by the per-item lookup across the whole batch — use the
     * pre-derived views ({@link FactoryItemsBatch#getTwins()}, {@link FactoryItemsBatch#getContextTwins()})
     * instead of re-collecting them from the items. Unlike the other lookuper hooks this one receives
     * the lookup params, because the matched links (and their dst twins) can only be bulk-resolved
     * per {@code linkedTwinByLinkId}. Default: no-op.
     */
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByLinkId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
    }

    /** Per-item lookup; null = not found (becomes an undefined value at the batch boundary). */
    protected abstract FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, UUID linkedTwinByLinkId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException;
}
