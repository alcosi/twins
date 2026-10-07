package org.twins.core.domain.factory;

import lombok.AccessLevel;
import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

/**
 * Context for a single pipeline-step {@link org.twins.core.featurer.factory.filler.Filler#fill} call:
 * the factory items of the step (order-preserving) plus derived views for bulk preloads in
 * {@code beforeFill} / {@code beforeLookup} hooks (fillers and field lookupers alike). Mirrors
 * {@code RecipientResolveBatch}: {@link #getTwins()} / {@link #getTwinIds()} / level-1
 * {@link #getContextTwins()} are populated incrementally in {@link #add} (plain field reads);
 * deeper context levels ({@link #getContextTwins(int)}) are computed on demand — only the rare
 * lookuper that actually walks deeper pays for them. Implementers must use these views instead of
 * re-collecting the same sets from the items per filler.
 * <p>Fillers write results into the items themselves, so unlike the notificator batches this object
 * carries no accumulator.
 */
@Getter
public class FactoryItemsBatch {
    private final List<FactoryItem> factoryItems = new ArrayList<>();
    private final List<TwinEntity> twins = new ArrayList<>();
    /**
     * -- GETTER --
     *  Level-1 context twins of the batch items — plain field read (derived in
     * ).
     */
    // level-1 context twins (the items' own context items' twins) — the common case, derived
    // incrementally in add() like twins/twinIds, so the no-arg getter is a plain field read.
    // Identity-deduped: multiplier copies share one context item, its twin is added once.
    private final Set<TwinEntity> contextTwins = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<UUID> twinIds = new HashSet<>();
    @Getter(AccessLevel.NONE)
    private final Set<FactoryItem> uniq = Collections.newSetFromMap(new IdentityHashMap<>()); // identity guard: FactoryItem equals is field-based and mutable

    public FactoryItemsBatch add(FactoryItem factoryItem) {
        if (factoryItem == null || !uniq.add(factoryItem)) {
            return this; // already registered — keeps twins/twinIds free of duplicates
        }
        factoryItems.add(factoryItem);
        TwinEntity twin = factoryItem.getTwin();
        if (twin != null) {
            twins.add(twin);
            if (twin.getId() != null)
                twinIds.add(twin.getId());
        }
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) {
            if (contextItem == null) {
                continue;
            }
            TwinEntity contextTwin = contextItem.getTwin();
            if (contextTwin != null) {
                contextTwins.add(contextTwin);
            }
        }
        return this;
    }

    /**
     * Context twins of the batch items, walking {@code level} levels down the context chain
     * (2 = additionally the context items' context twins — the "look deeper" step of
     * {@code FieldLookuperFromContextFieldsAndContextTwinDbFields}). Level 1 is the derived field;
     * deeper levels are computed on demand — a filler/lookuper bulk preload must call this once per
     * batch, never re-derive per item.
     */
    public Set<TwinEntity> getContextTwins(int level) {
        if (level == 1) {
            return contextTwins;
        }
        Set<TwinEntity> ret = Collections.newSetFromMap(new IdentityHashMap<>());
        ret.addAll(contextTwins);
        if (level > 1) {
            List<FactoryItem> contextItems = new ArrayList<>();
            for (FactoryItem factoryItem : factoryItems) {
                for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) {
                    if (contextItem != null) {
                        contextItems.add(contextItem);
                    }
                }
            }
            collectContextTwins(contextItems, 2, level, ret);
        }
        return ret;
    }

    private static void collectContextTwins(List<FactoryItem> items, int currentLevel, int maxLevel, Set<TwinEntity> acc) {
        if (currentLevel > maxLevel) {
            return;
        }
        for (FactoryItem item : items) {
            if (item == null) {
                continue;
            }
            List<FactoryItem> contextItems = item.getContextFactoryItemList();
            for (FactoryItem contextItem : contextItems) {
                if (contextItem == null) {
                    continue;
                }
                TwinEntity contextTwin = contextItem.getTwin();
                if (contextTwin != null) {
                    acc.add(contextTwin);
                }
            }
            collectContextTwins(contextItems, currentLevel + 1, maxLevel, acc);
        }
    }

    public boolean isEmpty() {
        return factoryItems.isEmpty();
    }

    public int size() {
        return factoryItems.size();
    }
}
