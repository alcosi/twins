package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByField extends FieldLookuper implements FieldLookuperLinked {
    @Autowired
    protected TwinLinkService twinLinkService;

    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        return lookupFieldValue(factoryItemsBatch, linkedTwinByTwinClassFieldId, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId)); // entity resolved once per batch
    }

    /**
     * Two-pass batch lookup: the linked twin is discovered per item (pass 1, isolated — navigation
     * errors fail only their item), then ONE bulk {@code preloadLinkedTwins} covers the whole batch
     * and every item's {@code readSource} is derived (isolated — e.g. the linked twin's head), then
     * ONE bulk {@code loadTwinFields} preloads the read sources, and the final field read runs per
     * item against the preloaded twin (pass 2, isolated). A null final value becomes an undefined
     * {@link FieldValue} — the not-found contract of the FieldLookuperNearest family.
     */
    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByTwinClassFieldId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        var ret = LookupResult.empty(factoryItemsBatch.size());
        beforeLookup(factoryItemsBatch, linkedTwinByTwinClassFieldId, lookupTwinClassField);
        var linkedTwinByItem = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (var factoryItem : factoryItemsBatch.getFactoryItems()) {
            try {
                linkedTwinByItem.put(factoryItem, linkedTwin(factoryItem, linkedTwinByTwinClassFieldId));
            } catch (ServiceException ex) {
                ret.failures().put(factoryItem, ex); // per-item isolation — the caller re-throws per item
            }
        }
        preloadLinkedTwins(linkedTwinByItem.values());
        var readSourceByItem = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (var entry : linkedTwinByItem.entrySet()) {
            try {
                readSourceByItem.put(entry.getKey(), readSource(entry.getValue()));
            } catch (ServiceException ex) {
                ret.failures().put(entry.getKey(), ex); // per-item isolation — the caller re-throws per item
            }
        }
        if (!readSourceByItem.isEmpty())
            twinService.loadTwinFields(readSourceByItem.values(), lookupTwinClassField); // one bulk load for the final reads
        for (var entry : readSourceByItem.entrySet()) {
            try {
                var value = lookupFieldValueOrNull(entry.getKey(), entry.getValue(), lookupTwinClassField);
                ret.values().put(entry.getKey(), value == null ? twinService.createFieldValue(lookupTwinClassField) : value); //create field as undefined
            } catch (ServiceException ex) {
                ret.failures().put(entry.getKey(), ex); // per-item isolation — the caller re-throws per item
            }
        }
        return ret;
    }

    /**
     * Override to bulk-load relations needed by the per-item navigation across the whole batch — use the
     * pre-derived views ({@link FactoryItemsBatch#getTwins()}, {@link FactoryItemsBatch#getContextTwins()})
     * instead of re-collecting them from the items. Default: no-op.
     */
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByTwinClassFieldId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
    }

    /**
     * Bulk-load the extra relations of the linked twins discovered by pass 1 (e.g. their heads) —
     * the targets are only known per item, so this is the bulk slot between the passes. Default: no-op.
     */
    protected void preloadLinkedTwins(Collection<TwinEntity> linkedTwins) throws ServiceException {
    }

    /**
     * The twin the lookup field will be read from — a further reference hop of the navigation
     * (e.g. the linked twin's head), resolved in memory after {@code preloadLinkedTwins}.
     * Default: the linked twin itself.
     */
    protected TwinEntity readSource(TwinEntity linkedTwin) throws ServiceException {
        return linkedTwin;
    }

    /** Pass 1: navigate from the item to the linked twin. */
    protected abstract TwinEntity linkedTwin(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId) throws ServiceException;

    /** Pass 2: read the lookup field from the preloaded read source; null = not found (becomes an undefined value). */
    protected abstract FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinEntity readSource, TwinClassFieldEntity lookupTwinClassField) throws ServiceException;
}
