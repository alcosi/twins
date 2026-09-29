package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

/**
 * Batch twin navigation driven by a CHAIN OF STAGES — see ai/plans/lookuper-navigation-stages.md.
 * Every navigation goes in stages ({@link NavigationStage}); the engine bulk-loads each stage for
 * the whole frontier, transitions the items in memory (isolated per item), then bulk-loads the
 * lookup field for the final frontier and reads it per item (null becomes an UNDEFINED value — the
 * not-found contract of the lookuper family). A failure at ANY point of the chain fails only its
 * item and lands in {@link LookupResult#failures()}.
 */
public abstract class FieldLookuperNavigated extends FieldLookuper {

    /** Entry twin of the navigation chain for the item; the hook may throw to fail the item. */
    protected abstract TwinEntity startTwin(FactoryItem factoryItem) throws ServiceException;

    /**
     * Final field read from the last stage's output twin; null = not found (the engine turns it into
     * an undefined value). Default: the freshest value (uncommitted output -> output links -> db).
     */
    protected FieldValue read(FactoryItem factoryItem, TwinEntity readSource, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return getFreshestValue(readSource, lookupTwinClassField, factoryItem.getFactoryContext());
    }

    /** The navigation engine: start -> stage chain -> terminal field bulk -> isolated read. */
    protected LookupResult navigate(FactoryItemsBatch batch, UUID lookupTwinClassFieldId, List<NavigationStage> stages) throws ServiceException {
        return navigate(batch, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId), stages);
    }

    /** Entity variant — the field entity is resolved by the caller (once per batch). */
    protected LookupResult navigate(FactoryItemsBatch batch, TwinClassFieldEntity lookupTwinClassField, List<NavigationStage> stages) throws ServiceException {
        var ret = LookupResult.empty(batch.size());
        var frontier = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                frontier.put(factoryItem, startTwin(factoryItem));
            } catch (ServiceException ex) {
                ret.failures().put(factoryItem, ex);
            }
        }
        for (NavigationStage stage : stages) {
            var loaded = nonNull(frontier.values());
            if (!loaded.isEmpty())
                stage.load(this, loaded); // ONE bulk query for the whole frontier
            var next = new LinkedHashMap<FactoryItem, TwinEntity>();
            for (var entry : frontier.entrySet()) {
                if (entry.getValue() == null) { // no navigation twin (map-based read) — carried over as is
                    next.put(entry.getKey(), null);
                    continue;
                }
                try {
                    next.put(entry.getKey(), stage.next(this, entry.getValue(), entry.getKey().getFactoryContext()));
                } catch (ServiceException ex) {
                    ret.failures().put(entry.getKey(), ex);
                }
            }
            frontier = next;
        }
        var readSources = nonNull(frontier.values());
        if (!readSources.isEmpty())
            twinService.loadTwinFields(readSources, lookupTwinClassField); // the terminal bulk — the read always hits a preloaded twin
        for (var entry : frontier.entrySet()) {
            if (entry.getValue() == null || ret.failures().containsKey(entry.getKey()))
                continue;
            try {
                var value = read(entry.getKey(), entry.getValue(), lookupTwinClassField);
                ret.values().put(entry.getKey(), value == null ? twinService.createFieldValue(lookupTwinClassField) : value); //create field as undefined
            } catch (ServiceException ex) {
                ret.failures().put(entry.getKey(), ex);
            }
        }
        return ret;
    }

    private static List<TwinEntity> nonNull(Collection<TwinEntity> twins) {
        var ret = new ArrayList<TwinEntity>(twins.size());
        for (TwinEntity twin : twins)
            if (twin != null)
                ret.add(twin);
        return ret;
    }
}
