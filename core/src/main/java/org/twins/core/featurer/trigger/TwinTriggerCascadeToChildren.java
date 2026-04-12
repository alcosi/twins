package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1508,
        name = "CascadeToChildren",
        description = "Cascade destination status to head-based children")
@RequiredArgsConstructor
public class TwinTriggerCascadeToChildren extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Max cascade depth (1 = direct children only, null = unlimited)", optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "DestinationStatusKey", description = "Status key (included/excluded) to find correct status per twin class", optional = true)
    public static final FeaturerParamString dstStatusKey = new FeaturerParamString("dstStatusKey");

    @FeaturerParam(name = "CascadeToSideEntities", description = "Also cascade to side entities via backward links (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeToSideEntities = new FeaturerParamBoolean("cascadeToSideEntities");

    @FeaturerParam(name = "CascadeUp", description = "When cascading to side entities, also recursively cascade up their head_twin_id hierarchy (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeUp = new FeaturerParamBoolean("cascadeUp");

    @FeaturerParam(name = "CascadeToForwardLinked", description = "Also cascade to forward-linked entities (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeToForwardLinked = new FeaturerParamBoolean("cascadeToForwardLinked");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinSearchService twinSearchService;
    @Lazy
    final TwinStatusService twinStatusService;
    final CascadeHelper cascadeHelper;
    @Lazy
    final LinkService linkService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        String dstStatusKeyValue = dstStatusKey.extract(properties);
        boolean shouldCascadeToSideEntities = cascadeToSideEntities.extract(properties);
        boolean shouldCascadeUp = cascadeUp.extract(properties);
        boolean shouldCascadeToForwardLinked = cascadeToForwardLinked.extract(properties);

        if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
            log.info("Running CascadeToChildren: twin {}, destination status key '{}', depth: {}, cascadeToSideEntities: {}, cascadeUp: {}, cascadeToForwardLinked: {}",
                    twinEntity.logShort(), dstStatusKeyValue, depthValue, shouldCascadeToSideEntities, shouldCascadeUp, shouldCascadeToForwardLinked);
        } else if (dstTwinStatus != null) {
            log.info("Running CascadeToChildren: twin {}, destination status '{}', depth: {}, cascadeToSideEntities: {}, cascadeUp: {}, cascadeToForwardLinked: {}",
                    twinEntity.logShort(), dstTwinStatus.getKey(), depthValue, shouldCascadeToSideEntities, shouldCascadeUp, shouldCascadeToForwardLinked);
        } else {
            log.warn("Running CascadeToChildren: twin {}, NO destination status provided!", twinEntity.logShort());
            return;
        }

        var search = new BasicSearch();
        search.setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new HierarchySearch()
                        .setDepth(depthValue)
                        .setIdList(Set.of(twinEntity.getId()))
        );
        List<TwinEntity> twinsToCascade = twinSearchService.findTwins(search);

        if (!twinsToCascade.isEmpty()) {
            if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
                // Find correct status for each twin class
                for (TwinEntity twin : twinsToCascade) {
                    TwinStatusEntity statusForTwin = twinStatusService.findByKey(twin.getTwinClassId(), dstStatusKeyValue);
                    if (statusForTwin != null) {
                        twinService.changeStatus(Collections.singletonList(twin), statusForTwin);
                        // Cascade to side entities (backward links) if enabled
                        if (shouldCascadeToSideEntities) {
                            cascadeToSideEntities(twin, dstStatusKeyValue, statusForTwin, shouldCascadeUp);
                        }
                        // Cascade to forward-linked entities if enabled
                        if (shouldCascadeToForwardLinked) {
                            cascadeToForwardLinked(twin, dstStatusKeyValue, statusForTwin, shouldCascadeUp);
                        }
                        // Cascade up the hierarchy (including backward links) if enabled
                        if (shouldCascadeUp) {
                            Set<UUID> visited = cascadeHelper.initVisitedSet(twin);
                            cascadeHelper.cascadeUpIncludingLinks(twin, dstStatusKeyValue, visited, "CascadeToChildren");
                        }
                    } else {
                        log.warn("Status '{}' not found for twin class {}, skipping twin {}", dstStatusKeyValue, twin.getTwinClassId(), twin.getId());
                    }
                }
            } else if (dstTwinStatus != null) {
                twinService.changeStatus(twinsToCascade, dstTwinStatus);
                // Cascade to side entities if enabled (cascadeUp only works with dstStatusKey)
                if (shouldCascadeToSideEntities) {
                    for (TwinEntity twin : twinsToCascade) {
                        cascadeToSideEntities(twin, null, dstTwinStatus, false);
                    }
                }
                // Cascade to forward-linked entities if enabled
                if (shouldCascadeToForwardLinked) {
                    for (TwinEntity twin : twinsToCascade) {
                        cascadeToForwardLinked(twin, null, dstTwinStatus, false);
                    }
                }
            }
        }
    }

    private void cascadeToSideEntities(TwinEntity twinEntity, String dstStatusKeyValue, TwinStatusEntity fallbackStatus, boolean cascadeUp) throws ServiceException {
        log.info("CascadeToSideEntities: starting for twin {}", twinEntity.logShort());

        // Find backward links (dst = current twin class)
        List<LinkEntity> sideLinks = linkService.findBackwardLinksForTwinClass(twinEntity.getTwinClass());

        log.info("CascadeToSideEntities: found {} side links for twin {}", sideLinks.size(), twinEntity.logShort());

        for (LinkEntity sideLink : sideLinks) {
            log.info("CascadeToSideEntities: processing link {} for twin {}", sideLink.getId(), twinEntity.logShort());
            cascadeViaLink(twinEntity, dstStatusKeyValue, fallbackStatus, sideLink, false, cascadeUp);
        }
    }

    private void cascadeToForwardLinked(TwinEntity twinEntity, String dstStatusKeyValue, TwinStatusEntity fallbackStatus, boolean cascadeUp) throws ServiceException {
        log.info("CascadeToForwardLinked: starting for twin {}", twinEntity.logShort());

        // Find forward links (src = current twin class)
        List<LinkEntity> forwardLinks = linkService.findForwardLinksForTwinClass(twinEntity.getTwinClass());

        log.info("CascadeToForwardLinked: found {} forward links for twin {}", forwardLinks.size(), twinEntity.logShort());

        for (LinkEntity forwardLink : forwardLinks) {
            log.info("CascadeToForwardLinked: processing link {} for twin {}", forwardLink.getId(), twinEntity.logShort());
            cascadeViaLink(twinEntity, dstStatusKeyValue, fallbackStatus, forwardLink, true, cascadeUp);
        }
    }

    private void cascadeViaLink(TwinEntity current, String dstStatusKeyValue, TwinStatusEntity fallbackStatus, LinkEntity linkEntity, boolean forward, boolean cascadeUp) throws ServiceException {
        log.info("cascadeViaLink: current={}, forward={}, linkId={}", current.logShort(), forward, linkEntity.getId());

        Collection<TwinEntity> targets = cascadeHelper.findLinkedTargets(linkEntity, current, forward);

        log.info("cascadeViaLink: found {} target twins", targets.size());

        if (!targets.isEmpty()) {
            if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
                // Find correct status for each twin class
                for (TwinEntity target : targets) {
                    log.info("cascadeViaLink: processing target twin {}", target.logShort());
                    TwinStatusEntity statusForTwin = twinStatusService.findByKey(target.getTwinClassId(), dstStatusKeyValue);
                    if (statusForTwin != null) {
                        log.info("cascadeViaLink: changing status of twin {} to {} ({})", target.logShort(), statusForTwin.getKey(), statusForTwin.getId());
                        twinService.changeStatus(Collections.singletonList(target), statusForTwin);
                        // Cascade up the hierarchy (including backward links) if enabled
                        if (cascadeUp) {
                            Set<UUID> visited = cascadeHelper.initVisitedSet(target);
                            cascadeHelper.cascadeUpIncludingLinks(target, dstStatusKeyValue, visited, "CascadeToChildren");
                        }
                    } else {
                        log.warn("Status '{}' not found for twin class {}, skipping twin {}", dstStatusKeyValue, target.getTwinClassId(), target.getId());
                    }
                }
            } else if (fallbackStatus != null) {
                twinService.changeStatus(targets, fallbackStatus);
            }
        }
    }
}
