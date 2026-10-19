package org.twins.core.domain.twinclass;

import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

/**
 * Accumulator for a {@link org.twins.core.featurer.fieldvalidator.FieldValidator} group that shares
 * the same {@code (featurerId, params)}. Owned by the caller; the featurer writes
 * {@link FieldValidateItem#getResult()} on each item.
 * <p>
 * Pre-derived views ({@link #getTwins()}, {@link #getHeadTwins()}) must be used by
 * {@code beforeValidate} preloads instead of re-collecting from items.
 */
public class FieldValidateBatch {
    private final List<FieldValidateItem> items = new ArrayList<>();
    private Collection<TwinEntity> twins;
    private Collection<TwinEntity> headTwins;

    public FieldValidateBatch add(FieldValidateItem item) {
        items.add(item);
        // Lazy caches of derived views — must invalidate when items change,
        // otherwise getTwins()/getHeadTwins() would return a stale projection.
        twins = null;
        headTwins = null;
        return this;
    }

    public FieldValidateBatch addAll(Collection<FieldValidateItem> items) {
        this.items.addAll(items);
        twins = null;
        headTwins = null;
        return this;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<FieldValidateItem> getItems() {
        return items;
    }

    /** Distinct twins referenced by items (identity-based; create path may lack twin ids). */
    public Collection<TwinEntity> getTwins() {
        if (twins == null) {
            IdentityHashMap<TwinEntity, Boolean> seen = new IdentityHashMap<>();
            List<TwinEntity> list = new ArrayList<>();
            for (FieldValidateItem item : items) {
                TwinEntity twin = item.getTwinEntity();
                if (twin != null && seen.put(twin, Boolean.TRUE) == null)
                    list.add(twin);
            }
            twins = list;
        }
        return twins;
    }

    /**
     * Distinct head twins already attached to {@link #getTwins()} via {@code loadHead}.
     * Empty until heads are loaded onto the twin entities.
     */
    public Collection<TwinEntity> getHeadTwins() {
        if (headTwins == null) {
            Map<UUID, TwinEntity> byId = new LinkedHashMap<>();
            for (TwinEntity twin : getTwins()) {
                TwinEntity head = twin.getHeadTwin();
                if (head != null && head.getId() != null)
                    byId.putIfAbsent(head.getId(), head);
            }
            headTwins = byId.values();
        }
        return headTwins;
    }

    /** Invalidate cached head view after {@code loadHead} so {@link #getHeadTwins()} re-derives. */
    public void invalidateHeadTwinsCache() {
        headTwins = null;
    }
}
