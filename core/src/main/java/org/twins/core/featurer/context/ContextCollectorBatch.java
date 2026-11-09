package org.twins.core.featurer.context;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.*;

/**
 * Context for a single {@link ContextCollector#collectDataBatch} call over one collector group
 * {@code (featurerId, params)}: the roots to collect for (as the per-root context accumulator) plus
 * the chunk-wide domain id, the derived twin / twin-class / business-account / twin-id collections, and an
 * i18n id accumulator.
 * <p>i18n is two-phase: {@link #addI18n} puts a {@code #i18n=<uuid>} placeholder into the context, the
 * caller then bulk-translates the registered ids per recipient locale ({@link #getI18nIds}) and
 * materializes the placeholders ({@link #isI18nPlaceholder}) at notify-event build time. This keeps locale
 * out of the thread-local {@code ApiUser} — required for batch, where the recipients of one chunk may have
 * different locales.
 * <p>All derived collections are populated incrementally in {@link #add} as roots are registered, so the
 * getters are plain field reads. {@code contextByRoot} is per collector group (a subset of the
 * notification chunk / face request), so the derived collections reflect that subset.
 */
@Getter
public class ContextCollectorBatch {
    private final UUID domainId;
    private Map<ContextRoot, Map<String, String>> contextByRoot = new HashMap<>();
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
        return add(new ContextRootHistory(history));
    }

    public ContextCollectorBatch add(TwinEntity twin) {
        return add(new ContextRoot(twin));
    }

    public ContextCollectorBatch add(ContextRoot root) {
        if (contextByRoot.containsKey(root)) {
            return this; // already registered — keeps twins/twinClasses free of duplicates
        }
        contextByRoot.computeIfAbsent(root, _ -> new HashMap<>());
        TwinEntity twin = root.getTwin();
        twinIds.add(twin.getId());
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
     * Puts an i18n placeholder into the root's context under {@code contextKey} and registers the id
     * for the per-locale bulk resolve: the placeholder is materialized with the recipient-locale
     * translation after collection (empty string when the locale or the translation is missing).
     *
     * @return the placeholder put into the context
     */
    public String addI18n(ContextRoot root, String contextKey, UUID i18nId) {
        i18nIds.add(i18nId);
        String placeholder = i18nPlaceholder(i18nId);
        contextByRoot.get(root).put(contextKey, placeholder);
        return placeholder;
    }

    /** The collected context for one root (null when the root was never added). */
    public Map<String, String> getContext(ContextRoot root) {
        return contextByRoot.get(root);
    }

    public Map<String, String> getContext(HistoryEntity history) {
        return contextByRoot.get(new ContextRootHistory(history));
    }

    public Map<String, String> getContext(TwinEntity twin) {
        return contextByRoot.get(new ContextRoot(twin));
    }

    public boolean isEmpty() {
        return contextByRoot.isEmpty();
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
