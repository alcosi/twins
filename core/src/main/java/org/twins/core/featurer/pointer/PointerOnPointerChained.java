package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinPointerId;
import org.twins.core.service.twin.TwinPointerService;

import java.util.*;

/**
 * General-purpose pointer chain: applies an ordered list of {@code twin_pointer_id}s in sequence,
 * where each hop's resolved twin becomes the source of the next hop. Unlike
 * {@link PointerOnLinkedChained} (which is specialized for link/head hops), this pointer composes
 * <em>any</em> pointer types (e.g. OnSingleChild -> OnGivenTwin -> OnLinkedTwin).
 *
 * <p>Cycle safety: this is the only pointer that recurses into other pointers (via
 * {@code subFeaturer.load(...)}); every other pointer resolves through direct service calls or the
 * hop helpers and never invokes another pointer. Hence a cycle can only arise from one
 * PointerOnPointerChained referencing another PointerOnPointerChained in its chain — which is
 * explicitly rejected in {@link #load}. No nesting counter needed: nesting is redundant anyway
 * (a nested chain can always be flattened into the parent's token list).
 */
@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3110,
        name = "Pointer chain pointed",
        description = "ordered chain of twin_pointer_id applied in sequence")
@RequiredArgsConstructor
public class PointerOnPointerChained extends Pointer {
    public static final int MAX_DEPTH = 10;

    @FeaturerParam(name = "Pointer ids",
            description = "ordered comma-separated twin_pointer_id list applied in sequence",
            order = 1)
    public static final FeaturerParamUUIDSet pointerIds = new FeaturerParamUUIDSetTwinsTwinPointerId("pointerIds");

    @Lazy
    private final TwinPointerService twinPointerService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        LinkedHashSet<UUID> pointers = pointerIds.extract(properties);
        if (pointers.isEmpty()) {
            return Map.of();
        }
        if (pointers.size() > MAX_DEPTH) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "PointerOnPointerChained chain too deep: " + pointers.size() + " > " + MAX_DEPTH);
        }
        Map<UUID, TwinEntity> frontier = identity(srcTwins);
        for (UUID subPointerId : pointers) {
            if (frontier.isEmpty()) {
                break;
            }
            TwinPointerEntity subPointer = twinPointerService.findEntitySafe(subPointerId);
            Pointer subFeaturer = featurerService.getFeaturer(subPointer.getPointerFeaturerId(), Pointer.class);
            // Cycle guard: only PointerOnPointerChained recurses into other pointers, so rejecting it
            // as a sub-pointer eliminates every possible A -> B -> A cycle (self-reference included).
            // Flatten nested chains into the parent token list instead.
            if (subFeaturer instanceof PointerOnPointerChained) {
                throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                        "PointerOnPointerChained must not reference another PointerOnPointerChained in its chain: " + subPointerId);
            }
            Collection<TwinEntity> currentTwins = new ArrayList<>(frontier.values());
            subFeaturer.load(subPointer, currentTwins); // batch; caches each resolved twin on currentTwins
            Map<UUID, TwinEntity> next = new HashMap<>(frontier.size());
            for (Map.Entry<UUID, TwinEntity> e : frontier.entrySet()) {
                TwinEntity resolved = e.getValue().getPointer(subPointerId);
                if (resolved != null) {
                    next.put(e.getKey(), resolved);
                }
            }
            frontier = next;
        }
        return frontier;
    }
}
