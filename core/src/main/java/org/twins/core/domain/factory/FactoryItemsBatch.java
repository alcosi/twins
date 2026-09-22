package org.twins.core.domain.factory;

import lombok.AccessLevel;
import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

/**
 * Context for a single pipeline-step {@link org.twins.core.featurer.factory.filler.Filler#fill} call:
 * the factory items of the step (order-preserving) plus the derived twins / twin-ids collections for
 * bulk preloads in {@code beforeFill}. Mirrors {@code RecipientResolveBatch}: the derived collections
 * are populated incrementally in {@link #add}, so the getters are plain field reads — implementers
 * must read them instead of re-collecting the same sets from the items per filler.
 * <p>Fillers write results into the items themselves, so unlike the notificator batches this object
 * carries no accumulator.
 */
@Getter
public class FactoryItemsBatch {
    private final List<FactoryItem> factoryItems = new ArrayList<>();
    private final List<TwinEntity> twins = new ArrayList<>();
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
        return this;
    }

    public boolean isEmpty() {
        return factoryItems.isEmpty();
    }

    public int size() {
        return factoryItems.size();
    }
}
