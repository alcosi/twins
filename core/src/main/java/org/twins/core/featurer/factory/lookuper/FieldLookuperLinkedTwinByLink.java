package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByLink extends FieldLookuper implements FieldLookuperLinked {
    @Autowired
    TwinLinkService twinLinkService;

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
        var ret = LookupResult.empty(factoryItemsBatch.size());
        beforeLookup(factoryItemsBatch, linkedTwinByLinkId, lookupTwinClassFieldId);
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            try {
                ret.values().put(factoryItem, lookupFieldValue(factoryItem, linkedTwinByLinkId, lookupTwinClassFieldId));
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
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
    }

    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException;
}
