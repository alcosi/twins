package org.twins.core.featurer.notificator.context;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;

import java.util.*;

/**
 * Context for a single {@link ContextCollector#collectDataBatch} call over one collector group
 * {@code (featurerId, params)}: the histories to collect for (as the per-history context accumulator) plus
 * the chunk-wide domain id, lazily-computed business account / twin id sets, and an i18n accumulator.
 * <p>i18n is two-phase: collectors register i18n ids via {@link #addI18n} (putting a placeholder into the
 * context), the caller resolves translations in bulk per locale afterwards and substitutes them. This keeps
 * locale out of the thread-local {@code ApiUser} — required for batch, where histories of one collector
 * group may belong to users with different locales.
 * <p>{@code contextByHistory} is per collector group (a subset of the notification chunk), so the lazy sets
 * are derived from its keySet — not from the whole chunk.
 */
@Getter
public class ContextCollectorBatch {
    private final UUID domainId;
    private Map<HistoryEntity, Map<String, String>> contextByHistory;
    private Set<UUID> businessAccountIds;
    private Set<UUID> twinIds;
    /** i18n accumulator: i18nId → references to substitute the translation into. */
    private final Map<UUID, List<I18nRef>> i18nRefs = new HashMap<>();

    public ContextCollectorBatch(UUID domainId) {
        this.domainId = domainId;
    }

    public ContextCollectorBatch add(HistoryEntity history) {
        if (contextByHistory == null) {
            contextByHistory = new HashMap<>();
        }
        contextByHistory.computeIfAbsent(history, _ -> new HashMap<>());
        return this;
    }

    /**
     * Register an i18n id to be resolved later (per locale) and put a placeholder into the context under
     * {@code contextKey}. The placeholder is replaced by the resolved translation in the post-collect phase.
     */
    public String addI18n(HistoryEntity history, String contextKey, UUID i18nId) {
        i18nRefs.computeIfAbsent(i18nId, _ -> new ArrayList<>()).add(new I18nRef(history, contextKey));
        return placeholder(i18nId);
    }

    /** Distinct i18n ids accumulated so far (to resolve in bulk). */
    public Set<UUID> getI18nIds() {
        return i18nRefs.keySet();
    }

    /** Distinct non-null {@code twin.ownerBusinessAccountId} values across {@code contextByHistory} keySet. */
    public Set<UUID> getBusinessAccountIds() {
        if (businessAccountIds == null) {
            businessAccountIds = new HashSet<>();
            collectTwins().forEach(t -> {
                if (t.getOwnerBusinessAccountId() != null) {
                    businessAccountIds.add(t.getOwnerBusinessAccountId());
                }
            });
        }
        return businessAccountIds;
    }

    /** Distinct non-null {@code twin.id} values across {@code contextByHistory} keySet. */
    public Set<UUID> getTwinIds() {
        if (twinIds == null) {
            twinIds = new HashSet<>();
            collectTwins().forEach(t -> {
                if (t.getId() != null) {
                    twinIds.add(t.getId());
                }
            });
        }
        return twinIds;
    }

    public boolean isEmpty() {
        return contextByHistory == null || contextByHistory.isEmpty();
    }

    private List<org.twins.core.dao.twin.TwinEntity> collectTwins() {
        List<org.twins.core.dao.twin.TwinEntity> twins = new ArrayList<>();
        for (HistoryEntity history : contextByHistory.keySet()) {
            if (history.getTwin() != null) {
                twins.add(history.getTwin());
            }
        }
        return twins;
    }

    private static String placeholder(UUID i18nId) {
        return "#i18n=" + i18nId;
    }

    /** A reference to where an i18n translation should land: (history, context key). */
    public record I18nRef(HistoryEntity history, String contextKey) {
    }
}
