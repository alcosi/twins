package org.twins.core.featurer.pointer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

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

    // ----------------------------------------------------------------------------------------------
    // Composite-navigation primitives — single-hop helpers shared by the compound pointers
    // (PointerOnLinkedTwinHead, PointerOnHeadLinkedTwin, PointerOnLinkedChained). Static, with the
    // services passed as arguments, so they neither force every Pointer subclass to inject
    // TwinLinkService/TwinService nor need a separate utility class. Each hop keeps the
    // srcId -> current mapping so multi-hop chains stay attributed to the original source twin.
    // ----------------------------------------------------------------------------------------------

    /** Builds the initial {@code srcId -> src} mapping preserving source order. */
    protected static Map<UUID, TwinEntity> identity(Collection<TwinEntity> srcTwins) {
        Map<UUID, TwinEntity> mapping = new LinkedHashMap<>();
        for (TwinEntity t : srcTwins) {
            mapping.put(t.getId(), t);
        }
        return mapping;
    }

    /**
     * Follows a single forward {@link TwinLinkEntity} of the given {@code linkId} for every current
     * twin. More than one forward link of the same type for a source yields
     * {@link ErrorCodeTwins#POINTER_NON_SINGLE}; no link drops the source from the result.
     */
    protected static Map<UUID, TwinEntity> followSingleForwardLink(TwinLinkService twinLinkService,
                                                                   Map<UUID, TwinEntity> srcById,
                                                                   UUID linkIdValue) throws ServiceException {
        if (srcById.isEmpty()) {
            return Map.of();
        }
        twinLinkService.loadTwinLinks(srcById.values());
        Map<UUID, TwinLinkEntity> linkBySrc = new HashMap<>();
        List<TwinLinkEntity> links = new ArrayList<>(srcById.size());
        for (Map.Entry<UUID, TwinEntity> e : srcById.entrySet()) {
            TwinEntity src = e.getValue();
            List<TwinLinkEntity> forwardLinks = src.getTwinLinks().getForwardLinks().getGrouped(linkIdValue);
            if (CollectionUtils.isEmpty(forwardLinks)) {
                continue;
            }
            if (forwardLinks.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE,
                        src.logShort() + " has " + forwardLinks.size() + " linked twins by link[" + linkIdValue + "]");
            }
            TwinLinkEntity link = forwardLinks.getFirst();
            linkBySrc.put(e.getKey(), link);
            links.add(link);
        }
        if (links.isEmpty()) {
            return Map.of();
        }
        twinLinkService.loadDstTwin(links);
        Map<UUID, TwinEntity> result = new HashMap<>(linkBySrc.size());
        linkBySrc.forEach((srcId, link) -> result.put(srcId, link.getDstTwin()));
        return result;
    }

    /** Resolves the immediate head twin of every current twin; sources with no head are dropped. */
    protected static Map<UUID, TwinEntity> toHead(TwinService twinService,
                                                   Map<UUID, TwinEntity> srcById) throws ServiceException {
        if (srcById.isEmpty()) {
            return Map.of();
        }
        twinService.loadHead(srcById.values());
        Map<UUID, TwinEntity> result = new HashMap<>(srcById.size());
        for (Map.Entry<UUID, TwinEntity> e : srcById.entrySet()) {
            TwinEntity head = e.getValue().getHeadTwin();
            if (head != null) {
                result.put(e.getKey(), head);
            }
        }
        return result;
    }
}
