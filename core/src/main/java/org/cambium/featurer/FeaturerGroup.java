package org.cambium.featurer;

import lombok.Getter;
import org.cambium.common.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;

/**
 * A group of items sharing the same {@code (featurerId, params)}; produced by
 * {@link #groupByFeaturerParams}. Resolve/collect the whole group with one featurer call.
 * {@link #getConfigKey()} is the precomputed {@link FeaturerService#toConfigKey(int, HashMap)} —
 * reuse it as a cache key instead of recomputing.
 */
@Getter
public final class FeaturerGroup<E> {
    private final int featurerId;
    private final HashMap<String, String> params;
    private final String configKey;
    private final List<E> items = new ArrayList<>();

    FeaturerGroup(int featurerId, HashMap<String, String> params, String configKey) {
        this.featurerId = featurerId;
        this.params = params;
        this.configKey = configKey;
    }

    FeaturerGroup<E> add(E item) {
        items.add(item);
        return this;
    }

    /**
     * Groups items that share the same featurer + params, so a batch featurer call (e.g.
     * {@code resolveBatch} / {@code collectDataBatch}) can be made once per unique
     * {@code (featurerId, canonical params)} pair instead of once per item. The dedup key is
     * {@link FeaturerService#toConfigKey(int, HashMap)} — order-independent and stable. Items with a
     * {@code null} featurerId are skipped.
     * <p>Featurer-specific facets (e.g. recipient include/exclude, accumulator shape) stay with the
     * caller — this helper only does the featurer/params grouping.
     */
    public static <E> Collection<FeaturerGroup<E>> groupByFeaturerParams(
            Collection<E> items,
            Function<E, Integer> featurerIdGetter,
            Function<E, HashMap<String, String>> paramsGetter) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        FeaturerParamGroupBuilder<E, E> builder = new FeaturerParamGroupBuilder<>(featurerIdGetter, paramsGetter);
        for (E item : items) {
            builder.add(item, item);
        }
        return builder.build();
    }

    public static <FH, E> FeaturerParamGroupBuilder<FH, E> builder(Function<FH, Integer> featurerIdGetter, Function<FH, HashMap<String, String>> paramsGetter) {
        return new FeaturerParamGroupBuilder<>(featurerIdGetter, paramsGetter);
    }

    public static <FH, E> FeaturerParamGroupBuilder<FH, E> builder(Function<FH, Integer> featurerIdGetter, Function<FH, HashMap<String, String>> paramsGetter, Class<E> clazz) {
        return new FeaturerParamGroupBuilder<>(featurerIdGetter, paramsGetter);
    }

    /**
     * Incremental companion to {@link #groupByFeaturerParams}: accumulates (featurerHolder, element)
     * pairs straight into {@code (featurerId, canonical params)} groups, so callers don't need an
     * intermediate work-unit wrapper list. The featurer id/params are extracted from the holder via
     * the constructor functions; the group key is
     * {@link FeaturerService#toConfigKey(int, HashMap)} — order-independent and stable. Holders with
     * a {@code null} featurerId are skipped.
     */
    public static final class FeaturerParamGroupBuilder<FH, E> {
        private final Function<FH, Integer> featurerIdGetter;
        private final Function<FH, HashMap<String, String>> paramsGetter;
        private final Map<String, FeaturerGroup<E>> groups = new LinkedHashMap<>();

        public FeaturerParamGroupBuilder(Function<FH, Integer> featurerIdGetter, Function<FH, HashMap<String, String>> paramsGetter) {
            this.featurerIdGetter = featurerIdGetter;
            this.paramsGetter = paramsGetter;
        }

        public FeaturerParamGroupBuilder<FH, E> add(FH featurerHolder, E element) {
            Integer featurerId = featurerIdGetter.apply(featurerHolder);
            if (featurerId == null) {
                return this;
            }
            HashMap<String, String> params = paramsGetter.apply(featurerHolder);
            groups.computeIfAbsent(FeaturerService.toConfigKey(featurerId, params),
                    key -> new FeaturerGroup<>(featurerId, params, key)).add(element);
            return this;
        }

        public boolean isEmpty() {
            return groups.isEmpty();
        }

        public Collection<FeaturerGroup<E>> build() {
            return groups.values();
        }
    }
}
