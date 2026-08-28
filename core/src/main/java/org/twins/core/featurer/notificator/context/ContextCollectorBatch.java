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
 * i18n id accumulator.
 * <p>i18n is two-phase: {@link #addI18n} puts a {@code #i18n=<uuid>} placeholder into the context, the
 * caller then bulk-translates the registered ids per recipient locale ({@link #getI18nIds}) and
 * materializes the placeholders ({@link #isI18nPlaceholder}) at notify-event build time. This keeps locale
 * out of the thread-local {@code ApiUser} — required for batch, where the recipients of one chunk may have
 * different locales.
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
    /** i18n ids put as placeholders by collectors — to resolve in bulk per locale afterwards. */
    private final Set<UUID> i18nIds = new LinkedHashSet<>();

    public ContextCollectorBatch(UUID domainId) {
        this.domainId = domainId;
    }

    public ContextCollectorBatch add(HistoryEntity history) {
        if (contextByHistory.containsKey(history)) {
            return this; // already registered — keeps twins/twinClasses free of duplicates
        }
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
     * Puts an i18n placeholder into the history's context under {@code contextKey} and registers the id
     * for the per-locale bulk resolve: the placeholder is materialized with the recipient-locale
     * translation after collection (empty string when the locale or the translation is missing).
     *
     * @return the placeholder put into the context
     */
    public String addI18n(HistoryEntity history, String contextKey, UUID i18nId) {
        i18nIds.add(i18nId);
        String placeholder = i18nPlaceholder(i18nId);
        contextByHistory.get(history).put(contextKey, placeholder);
        return placeholder;
    }

    public boolean isEmpty() {
        return contextByHistory.isEmpty();
    }

    /** Placeholder value encoding an i18n id pending its per-locale resolution (see {@link #addI18n}). */
    public static String i18nPlaceholder(UUID i18nId) {
        return PLACEHOLDER_PREFIX + i18nId;
    }

    /** Whether a context value is an i18n placeholder put by {@link #addI18n}. */
    public static boolean isI18nPlaceholder(String value) {
        return value != null && value.startsWith(PLACEHOLDER_PREFIX);
    }

    /** The i18n id encoded in a placeholder value (inverse of {@link #i18nPlaceholder}). */
    public static UUID i18nIdOfPlaceholder(String placeholder) {
        return UUID.fromString(placeholder.substring(PLACEHOLDER_PREFIX.length()));
    }

    private static final String PLACEHOLDER_PREFIX = "#i18n=";
}
