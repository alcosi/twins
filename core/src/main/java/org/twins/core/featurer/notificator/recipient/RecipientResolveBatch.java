package org.twins.core.featurer.notificator.recipient;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

/**
 * Context for a single {@link RecipientResolver#resolveBatch} call over one resolver group
 * {@code (featurerId, params)}: the histories to resolve (as the accumulator keySet) plus the
 * chunk-wide domain id and the derived twin / business-account / twin-id collections.
 * <p>All derived collections are populated incrementally in {@link #add} as histories are registered,
 * so the getters are plain field reads. {@code recipientIdsByHistory} is per resolver group (a subset
 * of the notification chunk), so the derived collections reflect that subset — not the whole chunk.
 */
@Getter
public class RecipientResolveBatch {
    private final UUID domainId;
    private Map<HistoryEntity, Set<UUID>> recipientIdsByHistory = new HashMap<>();
    private final List<TwinEntity> twins = new ArrayList<>();
    private final Set<UUID> businessAccountIds = new HashSet<>();
    private final Set<UUID> twinIds = new HashSet<>();

    public RecipientResolveBatch(UUID domainId) {
        this.domainId = domainId;
    }

    public RecipientResolveBatch add(HistoryEntity history) {
        if (recipientIdsByHistory.containsKey(history)) {
            return this; // already registered — keeps twins/twinIds free of duplicates
        }
        recipientIdsByHistory.computeIfAbsent(history, _ -> new HashSet<>());
        twinIds.add(history.getTwinId());
        TwinEntity twin = history.getTwin();
        twins.add(twin);
        if (twin.getOwnerBusinessAccountId() != null) {
            businessAccountIds.add(twin.getOwnerBusinessAccountId());
        }
        return this;
    }

    public boolean isEmpty() {
        return recipientIdsByHistory.isEmpty();
    }
}
