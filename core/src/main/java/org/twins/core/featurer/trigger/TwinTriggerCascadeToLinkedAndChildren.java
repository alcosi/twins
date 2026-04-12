package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cascade destination status to linked twins (via backward links) and their dependents.
 *
 * Performs two-level cascading:
 * 1. Finds twins linked via backward links (dst = current twin class)
 * 2. For each linked twin, cascades to:
 *    - Children via head_twin_id
 *    - Linked twins via forward links
 *
 * This is useful when a change needs to propagate to side entities and all their
 * dependencies in a single operation (trigger chains don't fire recursively).
 */
@Slf4j
@Component
@Featurer(id = 1511,
        name = "CascadeToLinkedAndChildren",
        description = "Cascade destination status to linked twins (via backward links) and their dependents (children via head, linked via forward links)")
@RequiredArgsConstructor
public class TwinTriggerCascadeToLinkedAndChildren extends TwinTrigger {

    @FeaturerParam(name = "DestinationStatusKey", description = "Status key (included/excluded) to find correct status per twin class", optional = true)
    public static final FeaturerParamString dstStatusKey = new FeaturerParamString("dstStatusKey");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinLinkService twinLinkService;
    final LinkRepository linkRepository;
    @Lazy
    final TwinClassService twinClassService;
    @Lazy
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinSearchService twinSearchService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        String dstStatusKeyValue = dstStatusKey.extract(properties);

        if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
            log.info("Running CascadeToLinkedAndChildren: twin {}, destination status key '{}'",
                    twinEntity.logShort(), dstStatusKeyValue);
        } else if (dstTwinStatus != null) {
            log.info("Running CascadeToLinkedAndChildren: twin {}, destination status '{}'",
                    twinEntity.logShort(), dstTwinStatus.getKey());
        } else {
            log.warn("Running CascadeToLinkedAndChildren: twin {}, NO destination status provided!", twinEntity.logShort());
            return;
        }

        // Load twin class extended hierarchy
        twinClassService.loadExtendsHierarchyChildClasses(twinEntity.getTwinClass());

        // Find backward links (dst = current twin class)
        Set<UUID> currentClassExtendedIds = twinEntity.getTwinClass().getExtendedClassIdSet();
        List<LinkEntity> sideLinks = linkRepository.findAll(
                (root, query, cb) -> root.get(LinkEntity.Fields.dstTwinClassId).in(currentClassExtendedIds)
        );

        for (LinkEntity sideLink : sideLinks) {
            cascadeViaLink(twinEntity, dstStatusKeyValue, dstTwinStatus, sideLink);
        }
    }

    private void cascadeViaLink(TwinEntity current, String dstStatusKeyValue, TwinStatusEntity fallbackStatus, LinkEntity linkEntity) throws ServiceException {
        Collection<TwinLinkEntity> links = twinLinkService.findTwinLinks(
                linkEntity,
                current,
                LinkService.LinkDirection.backward
        );

        if (links == null || links.isEmpty()) {
            return;
        }

        Collection<TwinEntity> linkedTwins = links.stream()
                .map(TwinLinkEntity::getSrcTwin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!linkedTwins.isEmpty()) {
            if (dstStatusKeyValue != null && !dstStatusKeyValue.isEmpty()) {
                // Find correct status for each twin class
                for (TwinEntity linkedTwin : linkedTwins) {
                    // Change linked twin status
                    TwinStatusEntity statusForLinkedTwin = twinStatusService.findByKey(linkedTwin.getTwinClassId(), dstStatusKeyValue);
                    if (statusForLinkedTwin != null) {
                        twinService.changeStatus(Collections.singletonList(linkedTwin), statusForLinkedTwin);
                    }

                    // Cascade to children (via head_twin_id)
                    cascadeToChildren(linkedTwin, dstStatusKeyValue);

                    // Cascade to forward-linked twins
                    cascadeToForwardLinked(linkedTwin, dstStatusKeyValue);
                }
            } else if (fallbackStatus != null) {
                twinService.changeStatus(linkedTwins, fallbackStatus);
            }
        }
    }

    private void cascadeToChildren(TwinEntity parent, String dstStatusKeyValue) throws ServiceException {
        // Find all children with head_twin_id = parent.id
        var search = new org.twins.core.domain.search.BasicSearch();
        search.setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new org.twins.core.domain.search.HierarchySearch()
                        .setDepth(1)
                        .setIdList(Set.of(parent.getId()))
        );
        List<TwinEntity> children = twinSearchService.findTwins(search);

        if (!children.isEmpty()) {
            for (TwinEntity child : children) {
                TwinStatusEntity statusForChild = twinStatusService.findByKey(child.getTwinClassId(), dstStatusKeyValue);
                if (statusForChild != null) {
                    twinService.changeStatus(Collections.singletonList(child), statusForChild);
                }
            }
        }
    }

    private void cascadeToForwardLinked(TwinEntity source, String dstStatusKeyValue) throws ServiceException {
        // Find forward links from source twin
        List<LinkEntity> forwardLinks = linkRepository.findAll(
                (root, query, cb) -> root.get(LinkEntity.Fields.srcTwinClassId).in(source.getTwinClass().getExtendedClassIdSet())
        );

        for (LinkEntity link : forwardLinks) {
            Collection<TwinLinkEntity> links = twinLinkService.findTwinLinks(
                    link,
                    source,
                    LinkService.LinkDirection.forward
            );

            if (links != null && !links.isEmpty()) {
                Collection<TwinEntity> forwardLinkedTwins = links.stream()
                        .map(TwinLinkEntity::getDstTwin)
                        .filter(Objects::nonNull)
                        .toList();

                for (TwinEntity forwardLinked : forwardLinkedTwins) {
                    TwinStatusEntity statusForForwardLinked = twinStatusService.findByKey(forwardLinked.getTwinClassId(), dstStatusKeyValue);
                    if (statusForForwardLinked != null) {
                        twinService.changeStatus(Collections.singletonList(forwardLinked), statusForForwardLinked);
                    }
                }
            }
        }
    }
}
