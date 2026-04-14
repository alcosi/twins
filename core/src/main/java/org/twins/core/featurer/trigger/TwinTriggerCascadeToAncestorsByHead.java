package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1510,
        name = "CascadeToAncestorsByHead",
        description = "Cascade status change up using head_twin_id")
@RequiredArgsConstructor
public class TwinTriggerCascadeToAncestorsByHead extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Hierarchy depth to search up (1 = direct parent only, -1 = unlimited)")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "StatusIds", description = "Map of twin class ID to status ID (format: class1_id:status_id,class2_id:status_id)")
    public static final FeaturerParamMap statusIds = new FeaturerParamMap("statusIds");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinStatusService twinStatusService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        log.warn("CascadeToAncestorsByHead: START - twinId={}, class={}",
            twinEntity.getId(), twinEntity.getTwinClassId());

        Integer depthValue = depth.extract(properties);

        if (depthValue == null || depthValue < 0) {
            depthValue = -1;
        }

        Map<UUID, UUID> statusMap = extractStatusMap(properties);

        log.warn("CascadeToAncestorsByHead: params - depth={}, statusMap={}",
            depthValue, statusMap);

        if (statusMap.isEmpty()) {
            log.warn("CascadeToAncestorsByHead: no valid status mappings");
            return;
        }

        List<TwinEntity> ancestors = findAncestorsByHead(twinEntity, depthValue);

        log.warn("CascadeToAncestorsByHead: found {} ancestors", ancestors.size());

        if (!ancestors.isEmpty()) {
            // Group by twin_class_id and update each group with its own status
            Map<UUID, List<TwinEntity>> ancestorsByClass = new HashMap<>();
            for (TwinEntity ancestor : ancestors) {
                ancestorsByClass.computeIfAbsent(ancestor.getTwinClassId(), k -> new ArrayList<>())
                        .add(ancestor);
            }

            for (Map.Entry<UUID, List<TwinEntity>> entry : ancestorsByClass.entrySet()) {
                UUID statusId = statusMap.get(entry.getKey());
                if (statusId != null) {
                    TwinStatusEntity status = twinStatusService.findEntitySafe(statusId);
                    twinService.changeStatus(entry.getValue(), status);
                    log.info("CascadeToAncestorsByHead: updated {} twins of class {} to status {}",
                        entry.getValue().size(), entry.getKey(), status.getKey());
                } else {
                    log.debug("CascadeToAncestorsByHead: no status mapping for class {}", entry.getKey());
                }
            }
        }
    }

    private List<TwinEntity> findAncestorsByHead(TwinEntity twinEntity, int maxDepth) {
        List<TwinEntity> ancestors = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        TwinEntity current = twinEntity;

        for (int i = 0; i < maxDepth || maxDepth < 0; i++) {
            if (current.getHeadTwinId() == null) {
                break;
            }

            UUID parentId = current.getHeadTwinId();
            if (visited.contains(parentId)) {
                log.warn("CascadeToAncestorsByHead: detected cycle at {}", parentId);
                break;
            }
            visited.add(parentId);

            try {
                current = twinService.findEntitySafe(parentId);
                ancestors.add(current);
            } catch (Exception e) {
                log.error("CascadeToAncestorsByHead: error loading parent {}", parentId, e);
                break;
            }
        }

        return ancestors;
    }

    private Map<UUID, UUID> extractStatusMap(Properties properties) {
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
