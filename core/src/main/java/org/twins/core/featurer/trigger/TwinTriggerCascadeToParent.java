package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cascade destination status to parent(s) via forward links.
 *
 * Finds forward links (src_twin_class_id = current twin class) and cascades
 * the destination status to the linked parent twins. Supports depth parameter
 * for multi-level cascade.
 */
@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1509,
        name = "CascadeToParent",
        description = "Cascade destination status to parent via forward link")
@RequiredArgsConstructor
public class TwinTriggerCascadeToParent extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Max cascade depth via links (1 = direct linked parents only, null = unlimited)", optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @FeaturerParam(name = "CascadeByHead", description = "Also cascade to parent via head_twin_id (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeByHead = new FeaturerParamBoolean("cascadeByHead");

    @FeaturerParam(name = "CascadeUp", description = "Recursively cascade up the head_twin_id hierarchy to all ancestors (true/false)", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean cascadeUp = new FeaturerParamBoolean("cascadeUp");

    @FeaturerParam(name = "DestinationStatusKey", description = "Status key (included/excluded) to find correct status per twin class", optional = true)
    public static final FeaturerParamString dstStatusKey = new FeaturerParamString("dstStatusKey");

    @FeaturerParam(name = "LinkId", description = "Filter cascade to specific link only (optional)", optional = true)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUID("linkId");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinLinkService twinLinkService;
    @Lazy
    final TwinStatusService twinStatusService;
    final CascadeHelper cascadeHelper;
    @Lazy
    final org.twins.core.service.link.LinkService linkService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        boolean shouldCascadeByHead = cascadeByHead.extract(properties);
        boolean shouldCascadeUp = cascadeUp.extract(properties);
        String dstStatusKeyValue = dstStatusKey.extract(properties);
        UUID linkIdValue = linkId.extract(properties);

        // Determine the status to cascade
        TwinStatusEntity statusToCascade;
        if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
            statusToCascade = twinStatusService.findByKey(twinEntity.getTwinClassId(), dstStatusKeyValue);
            if (statusToCascade == null) {
                log.warn("Status '{}' not found for twin class {}, skipping cascade", dstStatusKeyValue, twinEntity.getTwinClassId());
                return;
            }
            log.info("Running CascadeToParent: twin {}, destination status key '{}', depth: {}, cascadeByHead: {}, cascadeUp: {}, linkId: {}",
                    twinEntity.logShort(), dstStatusKeyValue, depthValue, shouldCascadeByHead, shouldCascadeUp, linkIdValue);
        } else if (dstTwinStatus != null) {
            statusToCascade = dstTwinStatus;
            log.info("Running CascadeToParent: twin {}, destination status '{}', depth: {}, cascadeByHead: {}, cascadeUp: {}",
                    twinEntity.logShort(), dstTwinStatus.getKey(), depthValue, shouldCascadeByHead, shouldCascadeUp);
        } else {
            log.warn("Running CascadeToParent: twin {}, NO destination status provided!", twinEntity.logShort());
            return;
        }

        // Cascade by head_twin_id if enabled (single level or recursive)
        if ((shouldCascadeByHead || shouldCascadeUp) && twinEntity.getHeadTwinId() != null) {
            Set<UUID> visited = cascadeHelper.initVisitedSet(twinEntity);
            if (shouldCascadeUp) {
                // Recursive cascade including backward links
                cascadeHelper.cascadeUpIncludingLinks(twinEntity, dstStatusKeyValue, visited, "CascadeToParent");
            } else {
                // Single level cascade via head_twin_id only
                cascadeHelper.cascadeUpToHeadParents(twinEntity, dstStatusKeyValue, visited, false,
                        twinService, twinStatusService, "CascadeToParent");
            }
        }

        // Find forward links (src = current twin class)
        List<LinkEntity> parentLinks = linkService.findForwardLinksForTwinClass(twinEntity.getTwinClass());

        // Filter by linkId if specified
        if (linkIdValue != null) {
            parentLinks = parentLinks.stream()
                    .filter(link -> link.getId().equals(linkIdValue))
                    .toList();
            log.debug("CascadeToParent: filtered to specific link {}", linkIdValue);
        }

        if (parentLinks.isEmpty()) {
            return;
        }

        if (depthValue == null || depthValue == 1) {
            // Direct linked parents only
            for (LinkEntity parentLink : parentLinks) {
                cascadeViaLink(twinEntity, dstStatusKeyValue, statusToCascade, parentLink, true);
            }
        } else {
            // Multi-level cascade - collect all reachable twins first, then change status with correct status per twin class
            Set<TwinEntity> allTargets = new HashSet<>();
            Set<UUID> visited = new HashSet<>();
            visited.add(twinEntity.getId());
            collectLinkedTwins(twinEntity, parentLinks, true, allTargets, visited, depthValue);

            if (!allTargets.isEmpty()) {
                // Find correct status for each twin class and change status
                if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
                    for (TwinEntity target : allTargets) {
                        TwinStatusEntity statusForTarget = twinStatusService.findByKey(target.getTwinClassId(), dstStatusKeyValue);
                        if (statusForTarget != null) {
                            twinService.changeStatus(Collections.singletonList(target), statusForTarget);
                            log.info("CascadeToParent: changed status of twin {} to {}", target.logShort(), statusForTarget.getKey());
                        } else {
                            log.warn("Status '{}' not found for twin class {}, skipping twin {}", dstStatusKeyValue, target.getTwinClassId(), target.getId());
                        }
                    }
                } else {
                    twinService.changeStatus(allTargets, statusToCascade);
                }
            }
        }
    }

    private void collectLinkedTwins(TwinEntity current, List<LinkEntity> links, boolean forward,
                                     Set<TwinEntity> collected, Set<UUID> visited, Integer remainingDepth) throws ServiceException {
        if (remainingDepth == null || remainingDepth <= 0) {
            return;
        }

        for (LinkEntity linkEntity : links) {
            Collection<TwinLinkEntity> twinLinks = twinLinkService.findTwinLinks(
                    linkEntity,
                    current,
                    forward ? LinkService.LinkDirection.forward : LinkService.LinkDirection.backward
            );

            if (twinLinks == null || twinLinks.isEmpty()) {
                continue;
            }

            Collection<TwinEntity> targets = twinLinks.stream()
                    .map(forward ? TwinLinkEntity::getDstTwin : TwinLinkEntity::getSrcTwin)
                    .filter(Objects::nonNull)
                    .filter(t -> !visited.contains(t.getId()))
                    .toList();

            for (TwinEntity target : targets) {
                if (visited.add(target.getId())) {
                    collected.add(target);
                    // Continue cascade for this target with reduced depth
                    Integer newDepth = remainingDepth == Integer.MAX_VALUE ? remainingDepth : remainingDepth - 1;
                    collectLinkedTwins(target, links, forward, collected, visited, newDepth);
                }
            }
        }
    }

    private void cascadeViaLink(TwinEntity current, String dstStatusKey, TwinStatusEntity fallbackStatus, LinkEntity linkEntity, boolean forward) throws ServiceException {
        Collection<TwinEntity> targets = cascadeHelper.findLinkedTargets(linkEntity, current, forward);

        if (!targets.isEmpty()) {
            cascadeHelper.changeStatusForEachTwinClass(targets, dstStatusKey, fallbackStatus, "CascadeToParent");
        }
    }
}
