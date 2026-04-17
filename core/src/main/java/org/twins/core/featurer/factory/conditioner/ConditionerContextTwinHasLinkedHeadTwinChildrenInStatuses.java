package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2442,
        name = "Context twin has linked head twin children in statuses",
        description = "Checks whether any context twin of the configured class" +
                " has a linked twin (by configured backward link) that has child twins" +
                " of the configured class in one of the configured statuses.")
@Slf4j
public class ConditionerContextTwinHasLinkedHeadTwinChildrenInStatuses extends Conditioner {
    @FeaturerParam(name = "Task twin class id", description = "", order = 1)
    public static final FeaturerParamUUID taskTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("taskTwinClassId");

    @FeaturerParam(name = "Supply portion to task link id", description = "", order = 2)
    public static final FeaturerParamUUID twinLinkId = new FeaturerParamUUIDTwinsLinkId("twinLinkId");

    @FeaturerParam(name = "Status ids", description = "", order = 3)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Context depth", description = "", order = 4, optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @Lazy
    @Autowired
    private TwinLinkService twinLinkService;

    @Lazy
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        Set<UUID> needLoadTwinsIds = new HashSet<>();
        collectContextTwinIdsByClass(factoryItem, taskTwinClassId.extract(properties), depth.extract(properties), needLoadTwinsIds);
        if (CollectionUtils.isEmpty(needLoadTwinsIds)) {
            return false;
        }

        UUID linkId = twinLinkId.extract(properties);
        Set<UUID> forwardTwinIds = new HashSet<>();
        for (UUID twinId : needLoadTwinsIds) {
            List<TwinLinkEntity> backwardLinks = twinLinkService.findTwinBackwardLinks(twinId);
            if (CollectionUtils.isEmpty(backwardLinks)) {
                continue;
            }
            for (TwinLinkEntity twinLinkEntity : backwardLinks) {
                if (linkId.equals(twinLinkEntity.getLinkId()) && twinLinkEntity.getSrcTwinId() != null) {
                    forwardTwinIds.add(twinLinkEntity.getSrcTwinId());
                }
            }
        }
        if (CollectionUtils.isEmpty(forwardTwinIds)) {
            return false;
        }

        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addHeadTwinId(forwardTwinIds);

        Set<UUID> extractedStatusIds = statusIds.extract(properties);
        if (CollectionUtils.isNotEmpty(extractedStatusIds)) {
            search.addStatusId(extractedStatusIds, false);
        }

        return twinSearchService.count(search) > 0;
    }

    private void collectContextTwinIdsByClass(FactoryItem factoryItem, UUID targetClassId, int depth, Set<UUID> result) throws ServiceException {
        if (factoryItem == null || depth < 0) {
            return;
        }
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) {
            if (contextItem.getTwin() != null && targetClassId.equals(contextItem.getTwin().getTwinClassId())) {
                result.add(contextItem.getTwin().getId());
            }
            if (depth > 0) {
                collectContextTwinIdsByClass(contextItem, targetClassId, depth - 1, result);
            }
        }
    }
}
