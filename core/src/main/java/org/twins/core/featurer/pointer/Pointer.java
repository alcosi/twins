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
     * {@link #load(Properties, Collection, boolean)} for the misses, then caches results on each src
     * twin (null result included, so a second pass for the same pointer is a no-op).
     * <p>
     * This is the primary tool for eliminating N+1 in FieldListenerService: N publishers with
     * one pointer = one SQL/lookup in subclass impl, not N.
     * <p>
     * {@link TwinPointerEntity#getOptional()} is delegated to the concrete impl so a per-twin
     * resolution failure (e.g. {@link ErrorCodeTwins#POINTER_NON_SINGLE} when a twin has more than
     * one forward link) can skip just that twin instead of poisoning the whole batch. The root never
     * swallows — if a subclass still throws, it propagates to the caller.
     */
    public void load(TwinPointerEntity pointer, Collection<TwinEntity> srcTwins) throws ServiceException {
        if (srcTwins == null || srcTwins.isEmpty()) return;
        UUID pointerId = pointer.getId();
        boolean optional = Boolean.TRUE.equals(pointer.getOptional());

        List<TwinEntity> misses = new ArrayList<>();
        for (TwinEntity src : srcTwins) {
            if (!src.hasPointer(pointerId)) {
                misses.add(src);
            }
        }
        if (misses.isEmpty()) return;

        Properties properties = featurerService.extractProperties(this, pointer.getPointerParams());
        Map<UUID, TwinEntity> loaded = load(properties, misses, optional);
        for (TwinEntity src : misses) {
            TwinEntity target = loaded == null ? null : loaded.get(src.getId());
            if (target == null && !optional) {
                throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL, "{} is not optional and has no target for {} ", pointer.logShort(), src.logShort());
            }
            src.addPointer(pointerId, target);
        }
    }

    /**
     * Strict ({@code optional = false}) convenience overload, used by direct subclass unit tests.
     */
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
        return load(properties, srcTwins, false);
    }

    /**
     * Concrete resolution. {@code optional = true} asks the impl to skip (resolve to null) any single
     * source twin whose resolution is ambiguous — e.g. more than one forward link — instead of
     * throwing {@link ErrorCodeTwins#POINTER_NON_SINGLE}; {@code optional = false} (strict) throws
     * as before.
     */
    protected abstract Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException;

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
     * {@link ErrorCodeTwins#POINTER_NON_SINGLE} when {@code optional = false}; with
     * {@code optional = true} the ambiguous source is skipped (dropped from the result) and the
     * rest of the batch is still resolved. No link drops the source from the result.
     */
    protected static Map<UUID, TwinEntity> followSingleForwardLink(TwinLinkService twinLinkService,
                                                                   Map<UUID, TwinEntity> srcById,
                                                                   UUID linkIdValue,
                                                                   boolean optional) throws ServiceException {
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
                if (optional) {
                    // per-twin skip: drop this ambiguous source, keep resolving the rest of the batch
                    log.warn("Optional pointer: {} has {} forward links by link[{}]; skipping this twin",
                            src.logShort(), forwardLinks.size(), linkIdValue);
                    continue;
                }
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
