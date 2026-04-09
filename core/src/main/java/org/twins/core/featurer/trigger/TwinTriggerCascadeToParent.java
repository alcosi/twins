package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
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
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1509,
        name = "CascadeToParent",
        description = "Cascade destination status to parent via forward link")
@RequiredArgsConstructor
public class TwinTriggerCascadeToParent extends TwinTrigger {

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinLinkService twinLinkService;
    final LinkRepository linkRepository;
    @Lazy
    final TwinClassService twinClassService;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        log.info("Running CascadeToParent: twin {}, destination status '{}", twinEntity.logShort(), dstTwinStatus.getKey());

        // Load twin class extended hierarchy
        twinClassService.loadExtendsHierarchyChildClasses(twinEntity.getTwinClass());

        // Find forward links (src = current twin class)
        Set<UUID> currentClassExtendedIds = twinEntity.getTwinClass().getExtendedClassIdSet();
        List<LinkEntity> parentLinks = linkRepository.findAll(
                (root, query, cb) -> root.get(LinkEntity.Fields.srcTwinClassId).in(currentClassExtendedIds)
        );

        for (LinkEntity parentLink : parentLinks) {
            cascadeViaLink(twinEntity, dstTwinStatus, parentLink, true);
        }
    }

    private void cascadeViaLink(TwinEntity current, TwinStatusEntity status, LinkEntity linkEntity, boolean forward) throws ServiceException {
        Collection<TwinLinkEntity> links = twinLinkService.findTwinLinks(
                linkEntity,
                current,
                forward ? LinkService.LinkDirection.forward : LinkService.LinkDirection.backward
        );

        if (links == null || links.isEmpty()) {
            return;
        }

        Collection<TwinEntity> targets = links.stream()
                .map(forward ? TwinLinkEntity::getDstTwin : TwinLinkEntity::getSrcTwin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            twinService.changeStatus(targets, status);
        }
    }
}
