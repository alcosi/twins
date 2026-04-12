package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for common cascade operations used by twin triggers.
 * Provides shared functionality for status cascading to avoid code duplication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CascadeHelper {

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinLinkService twinLinkService;
    @Lazy
    final LinkService linkService;

    /**
     * Recursively cascade status up the head_twin_id hierarchy.
     * Finds the correct status for each parent twin's class.
     *
     * @param currentTwin the current twin to cascade from
     * @param dstStatusKey the status key to find correct status per twin class
     * @param visited set of already visited twin IDs to prevent cycles
     * @param recursive if true, continue cascading up; if false, only direct parent
     * @param twinService service for finding twins
     * @param twinStatusService service for finding statuses
     * @param triggerName name of the trigger for logging
     */
    public void cascadeUpToHeadParents(
            TwinEntity currentTwin,
            String dstStatusKey,
            Set<UUID> visited,
            boolean recursive,
            TwinService twinService,
            TwinStatusService twinStatusService,
            String triggerName) {

        if (currentTwin.getHeadTwinId() == null) {
            return;
        }

        UUID headTwinId = currentTwin.getHeadTwinId();
        if (visited.contains(headTwinId)) {
            log.debug("{}: head twin {} already visited, skipping to prevent cycle", triggerName, headTwinId);
            return;
        }

        try {
            TwinEntity headTwin = twinService.findEntitySafe(headTwinId);

            // Find correct status for head twin's class
            TwinStatusEntity statusForHeadTwin;
            if (dstStatusKey != null && !dstStatusKey.isEmpty()) {
                statusForHeadTwin = twinStatusService.findByKey(headTwin.getTwinClassId(), dstStatusKey);
                if (statusForHeadTwin == null) {
                    log.warn("{}: Status '{}' not found for head twin class {}, stopping cascade",
                            triggerName, dstStatusKey, headTwin.getTwinClassId());
                    return;
                }
            } else {
                log.warn("{}: no dstStatusKey provided, skipping cascade", triggerName);
                return;
            }

            // Change status if different
            if (!headTwin.getTwinStatusId().equals(statusForHeadTwin.getId())) {
                twinService.changeStatus(Collections.singletonList(headTwin), statusForHeadTwin);
                log.info("{}: changed status of head twin {} to {}",
                        triggerName, headTwin.logShort(), statusForHeadTwin.getKey());
            } else {
                log.debug("{}: head twin {} already in status {}, skipping",
                        triggerName, headTwin.logShort(), statusForHeadTwin.getKey());
            }

            // Continue cascading up if recursive
            if (recursive) {
                visited.add(headTwinId);
                cascadeUpToHeadParents(headTwin, dstStatusKey, visited, true,
                        twinService, twinStatusService, triggerName);
            }

        } catch (Exception e) {
            log.warn("{}: failed to cascade to head twin {}: {}", triggerName, headTwinId, e.getMessage());
        }
    }

    /**
     * Recursively cascade status up the hierarchy including both head_twin_id parents
     * and backward-linked entities (like WORK_TYPE linked to TASK).
     *
     * @param currentTwin the current twin to cascade from
     * @param dstStatusKey the status key to find correct status per twin class
     * @param visited set of already visited twin IDs to prevent cycles
     * @param triggerName name of the trigger for logging
     * @throws ServiceException if cascade fails
     */
    public void cascadeUpIncludingLinks(
            TwinEntity currentTwin,
            String dstStatusKey,
            Set<UUID> visited,
            String triggerName) throws ServiceException {

        // Collect all parent candidates (head_twin_id + backward + forward links)
        Set<TwinEntity> parentsToCascade = new HashSet<>();

        // 1. Head parent via head_twin_id
        if (currentTwin.getHeadTwinId() != null && !visited.contains(currentTwin.getHeadTwinId())) {
            try {
                TwinEntity headParent = twinService.findEntitySafe(currentTwin.getHeadTwinId());
                parentsToCascade.add(headParent);
            } catch (Exception e) {
                log.warn("{}: failed to load head twin {}: {}", triggerName, currentTwin.getHeadTwinId(), e.getMessage());
            }
        }

        // 2. Backward-linked parents (dst_twin_class_id = current class)
        List<LinkEntity> backwardLinks = linkService.findBackwardLinksForTwinClass(currentTwin.getTwinClass());

        for (LinkEntity link : backwardLinks) {
            Collection<TwinLinkEntity> twinLinks = twinLinkService.findTwinLinks(
                    link,
                    currentTwin,
                    LinkService.LinkDirection.backward
            );
            parentsToCascade.addAll(collectLinkedTwins(twinLinks, TwinLinkEntity::getSrcTwin, visited));
        }

        // 3. Forward-linked parents (src_twin_class_id = current class, looking for entities we point to)
        List<LinkEntity> forwardLinks = linkService.findForwardLinksForTwinClass(currentTwin.getTwinClass());

        for (LinkEntity link : forwardLinks) {
            Collection<TwinLinkEntity> twinLinks = twinLinkService.findTwinLinks(
                    link,
                    currentTwin,
                    LinkService.LinkDirection.forward
            );
            parentsToCascade.addAll(collectLinkedTwins(twinLinks, TwinLinkEntity::getDstTwin, visited));
        }

        // Cascade to all found parents
        for (TwinEntity parent : parentsToCascade) {
            if (visited.contains(parent.getId())) {
                continue;
            }

            TwinStatusEntity statusForParent = twinStatusService.findByKey(parent.getTwinClassId(), dstStatusKey);
            if (statusForParent != null && !parent.getTwinStatusId().equals(statusForParent.getId())) {
                twinService.changeStatus(Collections.singletonList(parent), statusForParent);
                log.info("{}: changed status of parent {} to {}",
                        triggerName, parent.logShort(), statusForParent.getKey());
            }

            // Recursively cascade up from this parent
            visited.add(parent.getId());
            cascadeUpIncludingLinks(parent, dstStatusKey, visited, triggerName);
        }
    }

    /**
     * Collect linked twins from twin links, filtering by visited set.
     *
     * @param twinLinks the collection of twin links to process
     * @param twinMapper function to extract the target twin from TwinLinkEntity
     * @param visited set of already visited twin IDs to prevent cycles
     * @return collection of linked parents not yet visited
     */
    private Collection<TwinEntity> collectLinkedTwins(
            Collection<TwinLinkEntity> twinLinks,
            Function<TwinLinkEntity, TwinEntity> twinMapper,
            Set<UUID> visited) {
        if (twinLinks == null || twinLinks.isEmpty()) {
            return Collections.emptyList();
        }
        return twinLinks.stream()
                .map(twinMapper)
                .filter(Objects::nonNull)
                .filter(t -> !visited.contains(t.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Find linked target twins via a specific link entity and direction.
     * Returns all target twins (dst for forward, src for backward) from the given link.
     *
     * @param linkEntity the link to use for finding connections
     * @param currentTwin the current twin to find links from
     * @param forward true for forward direction (dst twins), false for backward (src twins)
     * @return collection of linked target twins, or empty collection if none found
     * @throws ServiceException if link lookup fails
     */
    public Collection<TwinEntity> findLinkedTargets(
            LinkEntity linkEntity,
            TwinEntity currentTwin,
            boolean forward) throws ServiceException {
        Collection<TwinLinkEntity> links = twinLinkService.findTwinLinks(
                linkEntity,
                currentTwin,
                forward ? LinkService.LinkDirection.forward : LinkService.LinkDirection.backward
        );

        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }

        return links.stream()
                .map(forward ? TwinLinkEntity::getDstTwin : TwinLinkEntity::getSrcTwin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Initialize visited set for cascade operations.
     * Adds the starting twin ID to prevent revisiting.
     *
     * @param startTwin the twin to start cascade from
     * @return initialized visited set
     */
    public Set<UUID> initVisitedSet(TwinEntity startTwin) {
        Set<UUID> visited = new HashSet<>();
        visited.add(startTwin.getId());
        return visited;
    }

    /**
     * Change status for each twin, finding the correct status per twin class.
     *
     * @param twins the collection of twins to change status for
     * @param dstStatusKey the status key to find correct status per twin class
     * @param triggerName name of the trigger for logging
     * @throws ServiceException if status change fails
     */
    public void changeStatusForEachTwinClass(Collection<TwinEntity> twins, String dstStatusKey, String triggerName) throws ServiceException {
        if (twins == null || twins.isEmpty()) {
            return;
        }

        for (TwinEntity target : twins) {
            TwinStatusEntity statusForTarget = twinStatusService.findByKey(target.getTwinClassId(), dstStatusKey);
            if (statusForTarget != null) {
                twinService.changeStatus(Collections.singletonList(target), statusForTarget);
                log.info("{}: changed status of twin {} to {}",
                        triggerName, target.logShort(), statusForTarget.getKey());
            } else {
                log.warn("{}: Status '{}' not found for twin class {}, skipping twin {}",
                        triggerName, dstStatusKey, target.getTwinClassId(), target.getId());
            }
        }
    }

    /**
     * Change status for each twin with a fallback status when dstStatusKey is not provided.
     *
     * @param twins the collection of twins to change status for
     * @param dstStatusKey the status key to find correct status per twin class (can be null/empty)
     * @param fallbackStatus the fallback status to use if dstStatusKey is not provided
     * @param triggerName name of the trigger for logging
     * @throws ServiceException if status change fails
     */
    public void changeStatusForEachTwinClass(Collection<TwinEntity> twins, String dstStatusKey, TwinStatusEntity fallbackStatus, String triggerName) throws ServiceException {
        if (twins == null || twins.isEmpty()) {
            return;
        }

        if (dstStatusKey != null && !dstStatusKey.isEmpty()) {
            changeStatusForEachTwinClass(twins, dstStatusKey, triggerName);
        } else if (fallbackStatus != null) {
            twinService.changeStatus(twins, fallbackStatus);
            log.info("{}: changed status of {} twins to {}",
                    triggerName, twins.size(), fallbackStatus.getKey());
        }
    }
}
