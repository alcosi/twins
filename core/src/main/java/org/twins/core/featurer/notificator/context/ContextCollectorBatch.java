package org.twins.core.featurer.notificator.context;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.*;

/**
 * Context for a single {@link ContextCollector#collectDataBatch} call over one collector group
 * {@code (featurerId, params)}: the histories to collect for (as the per-history context accumulator) plus
 * the chunk-wide domain id, the derived twin / twin-class / business-account / twin-id collections, and an
 * i18n accumulator.
 * <p>i18n is two-phase: collectors register i18n ids via {@link #addI18n} (putting a placeholder into the
 * context), the caller resolves translations in bulk per locale afterwards and substitutes them. This keeps
 * locale out of the thread-local {@code ApiUser} — required for batch, where histories of one collector
 * group may belong to users with different locales.
 * <p>All derived collections are populated incrementally in {@link #add} as histories are registered, so the
 * getters are plain field reads. {@code contextByHistory} is per collector group (a subset of the
 * notification chunk), so the derived collections reflect that subset — not the whole chunk.
 */
@Getter
public class ContextCollectorBatch {
    private final UUID domainId;
    private Map<HistoryEntity, Map<String, String>> contextByHistory = new HashMap<>();
    private final List<TwinEntity> twins = new ArrayList<>();
    private final List<TwinClassEntity> twinClasses = new ArrayList<>();
    private final Set<UUID> businessAccountIds = new HashSet<>();
    private final Set<UUID> twinIds = new HashSet<>();
    /**
     * i18n accumulator: i18nId → references to substitute the translation into.
     */
    private final Map<UUID, List<I18nRef>> i18nRefs = new HashMap<>();

    public ContextCollectorBatch(UUID domainId) {
        this.domainId = domainId;
    }

    public ContextCollectorBatch add(HistoryEntity history) {
        contextByHistory.computeIfAbsent(history, _ -> new HashMap<>());
        twinIds.add(history.getTwinId());
        TwinEntity twin = history.getTwin();
        twins.add(twin);
        if (twin.getOwnerBusinessAccountId() != null) {
            businessAccountIds.add(twin.getOwnerBusinessAccountId());
        }
        if (twin.getTwinClass() != null) {
            twinClasses.add(twin.getTwinClass());
        }
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

    /**
     * Distinct i18n ids accumulated so far (to resolve in bulk).
     */
    public Set<UUID> getI18nIds() {
        return i18nRefs.keySet();
    }

    public boolean isEmpty() {
        return contextByHistory.isEmpty();
    }

    private static String placeholder(UUID i18nId) {
        return "#i18n=" + i18nId;
    }

    /**
     * A reference to where an i18n translation should land: (history, context key).
     */
    public record I18nRef(HistoryEntity history, String contextKey) {
    }
}
