package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1510,
        name = "CascadeToSideEntities",
        description = "Cascade destination status to side entities via backward links")
@RequiredArgsConstructor
public class TwinTriggerByLink extends TwinTrigger {

    @FeaturerParam(name = "DestinationStatusKey", description = "Status key (included/excluded) to find correct status per twin class", optional = true)
    public static final FeaturerParamString dstStatusKey = new FeaturerParamString("dstStatusKey");

    @FeaturerParam(name = "CascadeByHead", description = "Also cascade to parent via head_twin_id (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeByHead = new FeaturerParamBoolean("cascadeByHead");

    @FeaturerParam(name = "CascadeUp", description = "Recursively cascade up the head_twin_id hierarchy to all ancestors (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeUp = new FeaturerParamBoolean("cascadeUp");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinStatusService twinStatusService;
    final CascadeHelper cascadeHelper;
    @Lazy
    final LinkService linkService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        String dstStatusKeyValue = dstStatusKey.extract(properties);
        boolean shouldCascadeByHead = cascadeByHead.extract(properties);
        boolean shouldCascadeUp = cascadeUp.extract(properties);

        if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
            log.info("Running CascadeToSideEntities: twin {}, destination status key '{}', cascadeByHead: {}, cascadeUp: {}",
                    twinEntity.logShort(), dstStatusKeyValue, shouldCascadeByHead, shouldCascadeUp);
        } else if (dstTwinStatus != null) {
            log.info("Running CascadeToSideEntities: twin {}, destination status '{}', cascadeByHead: {}, cascadeUp: {}",
                    twinEntity.logShort(), dstTwinStatus.getKey(), shouldCascadeByHead, shouldCascadeUp);
        } else {
            log.warn("Running CascadeToSideEntities: twin {}, NO destination status provided!", twinEntity.logShort());
            return;
        }

        // Find backward links (dst = current twin class)
        List<LinkEntity> sideLinks = linkService.findBackwardLinksForTwinClass(twinEntity.getTwinClass());

        for (LinkEntity sideLink : sideLinks) {
            cascadeViaLink(twinEntity, dstStatusKeyValue, dstTwinStatus, sideLink, false, shouldCascadeByHead, shouldCascadeUp);
        }
    }

    private void cascadeViaLink(TwinEntity current, String dstStatusKeyValue, TwinStatusEntity fallbackStatus, LinkEntity linkEntity, boolean forward, boolean cascadeByHead, boolean cascadeUp) throws ServiceException {
        Collection<TwinEntity> targets = cascadeHelper.findLinkedTargets(linkEntity, current, forward);

        if (!targets.isEmpty()) {
            if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
                // Find correct status for each twin class
                for (TwinEntity target : targets) {
                    TwinStatusEntity statusForTwin = twinStatusService.findByKey(target.getTwinClassId(), dstStatusKeyValue);
                    if (statusForTwin != null) {
                        twinService.changeStatus(Collections.singletonList(target), statusForTwin);
                        // Cascade to head parent if enabled
                        if (cascadeUp) {
                            Set<UUID> visited = cascadeHelper.initVisitedSet(target);
                            cascadeHelper.cascadeUpIncludingLinks(target, dstStatusKeyValue, visited, "CascadeToSideEntities");
                        } else if (cascadeByHead && target.getHeadTwinId() != null) {
                            Set<UUID> visited = cascadeHelper.initVisitedSet(target);
                            cascadeHelper.cascadeUpToHeadParents(target, dstStatusKeyValue, visited, false,
                                    twinService, twinStatusService, "CascadeToSideEntities");
                        }
                    } else {
                        log.warn("Status '{}' not found for twin class {}, skipping twin {}", dstStatusKeyValue, target.getTwinClassId(), target.getId());
                    }
                }
            } else if (fallbackStatus != null) {
                twinService.changeStatus(targets, fallbackStatus);
                // Cascade to head parent if enabled (only works with dstStatusKey, so this path doesn't support cascadeUp)
                if (cascadeByHead && !cascadeUp) {
                    for (TwinEntity target : targets) {
                        if (target.getHeadTwinId() != null) {
                            try {
                                TwinEntity headTwin = twinService.findEntitySafe(target.getHeadTwinId());
                                // Only change status if different
                                if (!headTwin.getTwinStatusId().equals(fallbackStatus.getId())) {
                                    twinService.changeStatus(Collections.singletonList(headTwin), fallbackStatus);
                                    log.info("CascadeToSideEntities: cascaded to head twin {} via head_twin_id", headTwin.logShort());
                                } else {
                                    log.debug("CascadeToSideEntities: head twin {} already in status {}, skipping", headTwin.logShort(), fallbackStatus.getKey());
                                }
                            } catch (Exception e) {
                                log.warn("CascadeToSideEntities: failed to cascade to head twin: {}", e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
}
