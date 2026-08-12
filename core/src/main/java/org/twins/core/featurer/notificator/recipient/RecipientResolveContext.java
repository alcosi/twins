package org.twins.core.featurer.notificator.recipient;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;

import java.util.*;

/**
 * Context for a single {@link RecipientResolver#resolveBatch} call over one resolver group
 * {@code (featurerId, params)}: the histories to resolve (as the accumulator keySet) plus the
 * chunk-wide domain id and a lazily-computed set of distinct business account ids.
 * <p>{@code recipientIdsByHistory} is per resolver group (a subset of the notification chunk), so
 * {@link #getBusinessAccountIds()} is derived from its keySet — not from the whole chunk — and
 * computed at most once per context.
 */
@Getter
public class RecipientResolveContext {
    private final UUID domainId;
    private Map<HistoryEntity, Set<UUID>> recipientIdsByHistory;
    private Set<UUID> businessAccountIds;
    private Set<UUID> twinIds;

    public RecipientResolveContext(UUID domainId) {
        this.domainId = domainId;
    }

    public RecipientResolveContext add(HistoryEntity history) {
        if (recipientIdsByHistory == null)
            recipientIdsByHistory = new HashMap<>();
        recipientIdsByHistory.computeIfAbsent(history, _ -> new HashSet<>());
        return this;
    }

    /**
     * Distinct non-null {@code twin.ownerBusinessAccountId} values across {@link #recipientIdsByHistory}
     * keySet. Computed lazily and cached; resolvers that don't need it never trigger the work.
     */
    public Set<UUID> getBusinessAccountIds() {
        if (businessAccountIds == null) {
            businessAccountIds = new HashSet<>();
            for (HistoryEntity history : recipientIdsByHistory.keySet()) {
                if (history.getTwin() != null) {
                    UUID businessAccountId = history.getTwin().getOwnerBusinessAccountId();
                    if (businessAccountId != null) {
                        businessAccountIds.add(businessAccountId);
                    }
                }
            }
        }
        return businessAccountIds;
    }

    /**
     * Distinct non-null {@code twin.id} values across {@link #recipientIdsByHistory} keySet (the spaces
     * to resolve space roles for). Computed lazily and cached; resolvers that don't need it never
     * trigger the work.
     */
    public Set<UUID> getTwinIds() {
        if (twinIds == null) {
            twinIds = new HashSet<>();
            for (HistoryEntity history : recipientIdsByHistory.keySet()) {
                if (history.getTwin() != null) {
                    UUID twinId = history.getTwin().getId();
                    if (twinId != null) {
                        twinIds.add(twinId);
                    }
                }
            }
        }
        return twinIds;
    }

    public boolean isEmpty() {
        return recipientIdsByHistory.isEmpty();
    }
}
