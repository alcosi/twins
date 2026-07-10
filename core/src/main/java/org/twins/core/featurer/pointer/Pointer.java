package org.twins.core.featurer.pointer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;

@FeaturerType(id = FeaturerTwins.TYPE_31,
        name = "Pointer",
        description = "Point from given twin to some other twin (linked, head or some other logic)")
@Slf4j
public abstract class Pointer extends FeaturerTwins {

    /**
     * Single-twin entry point with cache. On cache miss delegates to {@link #load(TwinPointerEntity, Collection)}
     * with a one-element list — the load() call populates the cache, then we read it back.
     * Idempotent within a tx.
     */
    public TwinEntity point(TwinPointerEntity pointer, TwinEntity srcTwinEntity) throws ServiceException {
        if (!srcTwinEntity.hasPointer(pointer.getId())) {
            load(pointer, List.of(srcTwinEntity));
        }
        return srcTwinEntity.getPointer(pointer.getId());
    }

    /**
     * Batch entry point. Resolves the subscriber twin for every src publisher twin in one pass
     * using a single subclass-defined lookup. Filters out already-cached entries, calls
     * {@link #load(Properties, Collection)} for the misses, then caches results on each src
     * twin (null result included, so a second pass for the same pointer is a no-op).
     * <p>
     * This is the primary tool for eliminating N+1 in FieldListenerService: N publishers with
     * one pointer = one SQL/lookup in subclass impl, not N.
     */
    public void load(TwinPointerEntity pointer, Collection<TwinEntity> srcTwins) throws ServiceException {
        if (srcTwins == null || srcTwins.isEmpty()) return;
        UUID pointerId = pointer.getId();

        List<TwinEntity> misses = new ArrayList<>();
        for (TwinEntity src : srcTwins) {
            if (!src.hasPointer(pointerId)) {
                misses.add(src);
            }
        }
        if (misses.isEmpty()) return;

        Properties properties = featurerService.extractProperties(this, pointer.getPointerParams());
        Map<UUID, TwinEntity> loaded = load(properties, misses);
        for (TwinEntity src : misses) {
            TwinEntity target = loaded == null ? null : loaded.get(src.getId());
            src.addPointer(pointerId, target);
        }
    }

    protected abstract Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException;
}
