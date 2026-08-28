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
@Featurer(id = FeaturerTwins.ID_3104,
        name = "Direct single child",
        description = "")
@RequiredArgsConstructor
public class PointerOnSingleChild extends Pointer {
    @FeaturerParam(name = "Child twin class", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("childTwinClassId");

    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        if (srcTwins.isEmpty()) return Map.of();
        UUID childClassId = twinClassId.extract(properties);
        BasicSearch batchSearch = new BasicSearch();
        batchSearch
                .addHeadTwinId(srcTwins.stream().map(TwinEntity::getId).collect(Collectors.toList()))
                .addTwinClassExtendsHierarchyContainsId(childClassId);
        List<TwinEntity> allChildren = twinSearchService.findTwins(batchSearch);
        Map<UUID, List<TwinEntity>> byHead = allChildren.stream()
                .collect(Collectors.groupingBy(TwinEntity::getHeadTwinId));
        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        for (TwinEntity src : srcTwins) {
            List<TwinEntity> children = byHead.get(src.getId());
            if (CollectionUtils.isEmpty(children)) {
                continue;
            }
            if (children.size() > 1) {
                if (optional) {
                    log.warn("Optional pointer: {} has {} child twins of class[{}]; skipping this twin", src.logShort(), children.size(), childClassId);
                    continue;
                }
                throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE,
                        src.logShort() + " has " + children.size() + " child twins of class[" + childClassId + "]");
            }
            result.put(src.getId(), children.getFirst());
        }
        return result;
    }
}
