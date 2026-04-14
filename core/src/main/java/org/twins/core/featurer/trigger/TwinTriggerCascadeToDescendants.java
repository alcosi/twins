package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1508,
        name = "CascadeToDescendants",
        description = "Cascade status change down using head_twin_id recursion")
@RequiredArgsConstructor
public class TwinTriggerCascadeToDescendants extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Hierarchy depth to search (1 = direct children only, -1 = unlimited)")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "StatusIds", description = "Map of twin class ID to status ID (format: class1_id=>status_id,class2_id=>status_id)")
    public static final FeaturerParamMap statusIds = new FeaturerParamMap("statusIds");

    @Lazy
    final TwinSearchService twinSearchService;
    @Lazy
    final TwinService twinService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);

        if (depthValue == null || depthValue < 0) {
            depthValue = HierarchySearch.INCLUDE_SELF;
        }

        Map<UUID, UUID> statusMap = extractLinkReplaceMap(properties);

        if (statusMap.isEmpty()) {
            log.warn("CascadeToDescendants: no valid status mappings");
            return;
        }

        var search = new BasicSearch();
        search.setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new HierarchySearch()
                        .setDepth(depthValue)
                        .setIdList(Collections.singleton(twinEntity.getId()))
        );

        List<TwinEntity> descendants = twinSearchService.findTwins(search);

        if (!descendants.isEmpty()) {
            // Group by twin_class_id and update each group with its own status
            Map<UUID, List<TwinEntity>> descendantsByClass = new HashMap<>();
            for (TwinEntity descendant : descendants) {
                descendantsByClass.computeIfAbsent(descendant.getTwinClassId(), k -> new ArrayList<>())
                        .add(descendant);
            }

            for (Map.Entry<UUID, List<TwinEntity>> entry : descendantsByClass.entrySet()) {
                UUID statusId = statusMap.get(entry.getKey());
                if (statusId != null) {
                     for (TwinEntity twin : entry.getValue()) {
                         twin.setTwinStatusId(statusId);
                     }
                } else {
                    log.debug("CascadeToDescendants: no status mapping for class {}", entry.getKey());
                }
            }
            twinService.saveSafe(descendants);
        }
    }

    private Map<UUID, UUID> extractLinkReplaceMap(Properties properties) {
        var extracted = statusIds.extract(properties);

        if (extracted.isEmpty()) {
            return Collections.emptyMap();
        }

        return extracted.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> UUID.fromString(entry.getValue())
                ));
    }
}
