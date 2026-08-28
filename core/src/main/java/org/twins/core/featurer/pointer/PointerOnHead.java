package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.LTreeUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3102,
        name = "Head twin pointed",
        description = "")
@RequiredArgsConstructor
public class PointerOnHead extends Pointer {
    @FeaturerParam(name = "Depth", description = "head levels to go up; default 1", order = 1, optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @Lazy
    private final TwinService twinService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        if (depthValue == null) {
            depthValue = 1; // default per @FeaturerParam(defaultValue = "1")
        }

        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        if (depthValue == 1) {
            // depth = 1: batched lookup of the immediate head (load() skips already-loaded heads).
            return toHead(twinService, identity(srcTwins));
        }

        // depth > 1: walk the ALREADY-LOADED headTwin references (no loadHead, no DB call) as far as
        // they go; only if the walk falls short, jump through the hierarchy tree and load just the
        // target twin. No DB roundtrip at all while the answer is already in memory.
        Map<UUID, UUID> needLoad = new HashMap<>(); // srcId -> target ancestor id
        for (TwinEntity src : srcTwins) {
            TwinEntity current = src;
            int walked = 0;
            while (walked < depthValue && current.getHeadTwin() != null) {
                current = current.getHeadTwin();
                walked++;
            }
            if (walked == depthValue) {
                result.put(src.getId(), current); // resolved from memory — no DB query
                continue;
            }
            // walk stopped at a twin with no loaded head: resolve the remaining levels through its
            // hierarchy tree (ltree). reverse=true -> index N is the N-th ancestor (1=head, 2=grandparent, ...).
            UUID targetId = LTreeUtils.uuidByIndex(current.getHierarchyTree(), true, depthValue - walked);
            if (targetId != null) {
                needLoad.put(src.getId(), targetId);
            }
            // hierarchy too short / missing -> source resolves to null (left absent from the result)
        }
        if (!needLoad.isEmpty()) {
            Kit<TwinEntity, UUID> loaded = twinService.findEntitiesSafe(needLoad.values());
            needLoad.forEach((srcId, targetId) -> result.put(srcId, loaded.get(targetId)));
        }
        return result;
    }
}
