package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

/**
 * Batch twin NAVIGATION driven by a chain of stages — the linked family only; the nearest family
 * (no navigation, just a per-item read) lives in {@link FieldLookuperNearest}. See
 * ai/plans/lookuper-navigation-stages.md. The engine runs four phases:
 * <ol>
 *   <li>{@code start}: resolve each item's entry twin ({@link #startTwin}, isolated per item),</li>
 *   <li>{@code stages}: per stage ONE bulk load for the whole frontier
 *       ({@link NavigationStage#load}) + an isolated per-item in-memory transition
 *       ({@link NavigationStage#next}),</li>
 *   <li>{@link #beforeRead}: ONE bulk for the read sources — by default the lookup field itself,</li>
 *   <li>{@code read}: the isolated per-item {@link #read}; a null value becomes an UNDEFINED value.</li>
 * </ol>
 * A failure at ANY phase fails only its item and lands in {@link LookupResult#failures()} —
 * runtime exceptions included, matching the old per-item caller. The bulk phases
 * ({@code stage.load}, {@link #beforeRead}) are NOT isolated: a failure there is batch-level
 * and aborts the whole lookup.
 */
public abstract class FieldLookuperNavigated extends FieldLookuper {

    /** Entry twin of the navigation chain for the item; may throw to fail the item. */
    protected abstract TwinEntity startTwin(FactoryItem factoryItem) throws ServiceException;

    /**
     * Final field read from the last stage's output twin; null = not found (the engine turns it into
     * an undefined value). Default: the freshest value (uncommitted output -> output links -> db).
     * <p>
     * PAIRED with {@link #beforeRead}: a read that hits the twin field store
     * ({@code getTwinFieldValue} / the freshest db fallback) lazily loads PER ITEM unless the sources
     * are preloaded — so a read and its preload are overridden TOGETHER.
     */
    protected FieldValue read(FactoryItem factoryItem, TwinEntity readSource, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return getFreshestValue(readSource, lookupTwinClassField, factoryItem.getFactoryContext());
    }

    /**
     * ONE bulk for the whole read phase, right before {@link #read}. Default: preload the lookup
     * field for the read sources (the default freshest/db reads need it). Override TOGETHER with
     * {@code read}: a read that does not touch the twin field store overrides this to a no-op (or
     * to its own bulk).
     */
    protected void beforeRead(Collection<TwinEntity> readSources, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        if (!readSources.isEmpty())
            twinService.loadTwinFields(readSources, lookupTwinClassField);
    }

    /** The navigation engine: start -> stage chain -> read bulk -> isolated read. */
    protected LookupResult navigate(FactoryItemsBatch batch, UUID lookupTwinClassFieldId, List<NavigationStage> stages) throws ServiceException {
        return navigate(batch, twinClassFieldService.findEntitySafe(lookupTwinClassFieldId), stages);
    }

    /** Entity variant — the field entity is resolved by the caller (once per batch). */
    protected LookupResult navigate(FactoryItemsBatch batch, TwinClassFieldEntity lookupTwinClassField, List<NavigationStage> stages) throws ServiceException {
        var ret = LookupResult.empty(batch.size());
        var frontier = collectStarts(batch, ret);
        for (NavigationStage stage : stages)
            frontier = advanceStage(stage, frontier, ret);
        beforeRead(nonNull(frontier.values()), lookupTwinClassField);
        readEach(frontier, lookupTwinClassField, ret);
        return ret;
    }

    /** Phase 1: per-item start twins, isolated — a failing start fails only its item. */
    private LinkedHashMap<FactoryItem, TwinEntity> collectStarts(FactoryItemsBatch batch, LookupResult ret) throws ServiceException {
        var frontier = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                frontier.put(factoryItem, startTwin(factoryItem));
            } catch (Exception ex) {
                ret.failures().put(factoryItem, LookupResult.asFailure(ex));
            }
        }
        return frontier;
    }

    /** Phase 2: ONE stage bulk for the frontier, then the isolated per-item in-memory transition. */
    private LinkedHashMap<FactoryItem, TwinEntity> advanceStage(NavigationStage stage, LinkedHashMap<FactoryItem, TwinEntity> frontier, LookupResult ret) throws ServiceException {
        var loaded = nonNull(frontier.values());
        if (!loaded.isEmpty())
            stage.load(this, loaded); // ONE bulk query for the whole frontier
        var next = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (var entry : frontier.entrySet()) {
            try {
                next.put(entry.getKey(), stage.next(this, entry.getValue(), entry.getKey().getFactoryContext()));
            } catch (Exception ex) {
                ret.failures().put(entry.getKey(), LookupResult.asFailure(ex));
            }
        }
        return next;
    }

    /** Phase 4: the isolated per-item read; a null value becomes an UNDEFINED value. */
    private void readEach(LinkedHashMap<FactoryItem, TwinEntity> frontier, TwinClassFieldEntity lookupTwinClassField, LookupResult ret) throws ServiceException {
        for (var entry : frontier.entrySet()) {
            if (ret.failures().containsKey(entry.getKey()))
                continue; // the item already failed somewhere along the chain
            try {
                var value = read(entry.getKey(), entry.getValue(), lookupTwinClassField);
                ret.values().put(entry.getKey(), value == null ? twinService.createFieldValue(lookupTwinClassField) : value); //create field as undefined
            } catch (Exception ex) {
                ret.failures().put(entry.getKey(), LookupResult.asFailure(ex));
            }
        }
    }

    private static List<TwinEntity> nonNull(Collection<TwinEntity> twins) {
        var ret = new ArrayList<TwinEntity>(twins.size());
        for (TwinEntity twin : twins)
            if (twin != null)
                ret.add(twin);
        return ret;
    }
}
