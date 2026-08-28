package org.twins.core.featurer.pointer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3105,
        name = "Direct single grandchild",
        description = "")
@RequiredArgsConstructor
public class PointerOnSingleGrandChild extends Pointer {
    @FeaturerParam(name = "Grandchild twin class", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("grandChildTwinClassId");

    @Lazy
    private final TwinSearchService twinSearchService;

    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        if (srcTwins.isEmpty()) return Map.of();
        UUID grandChildClassId = twinClassId.extract(properties);
        List<UUID> srcTwinIds = srcTwins.stream().map(TwinEntity::getId).collect(Collectors.toList());
        BasicSearch batchSearch = new BasicSearch();
        batchSearch
                .addHierarchyTreeContainsId(srcTwinIds)
                .addTwinClassExtendsHierarchyContainsId(grandChildClassId);
        List<TwinEntity> allGrandchildren = twinSearchService.findTwins(batchSearch);
        if (CollectionUtils.isEmpty(allGrandchildren)) {
            return Map.of();
        }
        // A grandchild's hierarchy_tree lists every ancestor; attribute each grandchild to every
        // src twin whose id appears in its ancestor set. The same grandchild may legitimately be
        // attributed to multiple src twins when their subtrees overlap (src1 < src2 in the
        // hierarchy), but each individual src must still resolve to at most one.
        Set<UUID> srcTwinIdSet = new HashSet<>(srcTwinIds);
        Map<UUID, List<TwinEntity>> bySrc = new HashMap<>();
        for (TwinEntity grandchild : allGrandchildren) {
            Set<UUID> ancestors = grandchild.getHeadTwinsIdSet();
            if (ancestors == null) {
                continue;
            }
            for (UUID srcId : srcTwinIdSet) {
                if (ancestors.contains(srcId)) {
                    bySrc.computeIfAbsent(srcId, k -> new ArrayList<>()).add(grandchild);
                }
            }
        }
        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        for (TwinEntity src : srcTwins) {
            List<TwinEntity> grandchildren = bySrc.get(src.getId());
            if (CollectionUtils.isEmpty(grandchildren)) {
                continue;
            }
            if (grandchildren.size() > 1) {
                if (optional) {
                    log.warn("Optional pointer: {} has {} grandchild twins of class[{}]; skipping this twin", src.logShort(), grandchildren.size(), grandChildClassId);
                    continue;
                }
                throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE,
                        src.logShort() + " has " + grandchildren.size() + " grandchild twins of class[" + grandChildClassId + "]");
            }
            result.put(src.getId(), grandchildren.getFirst());
        }
        return result;
    }
}
