package org.twins.core.featurer.factory.multiplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves “head” twins via a forward link to the factory input, then loads their child twins (by {@code headTwinId})
 * with optional status filtering. Selection logic matches {@link TwinRepository#updateTwinStatusByLinkAndHeadTwinChildren}
 * but returns entities instead of updating.
 */
@Slf4j
@Component
@Featurer(
        id = FeaturerTwins.ID_2214,
        name = "Isolated children by linked head twin",
        description = "For each input twin: via link (src→dst=input) collect src twins as heads, then output twins whose "
                + "headTwinId is one of those heads, filtered by statusIds (JPQL in TwinRepository)."
)
@RequiredArgsConstructor
public class MultiplierIsolatedChildrenByLinkedHeadTwin extends Multiplier {

    @FeaturerParam(name = "Link id", description = "Forward link: src twin is head candidate, dst twin is factory input", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Status ids", description = "Statuses of child twins (by head). If empty — any status", order = 2)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Exclude statuses", description = "Exclude(true)/Include(false) twinStatusId filter", order = 3, defaultValue = "false")
    public static final FeaturerParamBoolean excludeStatuses = new FeaturerParamBoolean("excludeStatuses");

    private final TwinRepository twinRepository;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        UUID ownerBusinessAccountId = factoryContext.getRunLimitedByOwnerBusinessAccount();
        var statusIdSet = statusIds.extract(properties);
        var uuidLink = linkId.extract(properties);
        boolean exclude = excludeStatuses.extract(properties);

        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            List<TwinEntity> children = loadChildren(List.of(inputTwin.getId()), uuidLink, ownerBusinessAccountId, statusIdSet, exclude);
            if (CollectionUtils.isEmpty(children)) {
                log.warn("{} no child twins for heads linked to input {}", inputTwin.logShort(), inputTwin.getId());
                continue;
            }
            for (TwinEntity child : children) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate
                        .setDbTwinEntity(child)
                        .setTwinEntity(child.clone());
                ret.add(new FactoryItem()
                        .setOutput(twinUpdate)
                        .setContextFactoryItemList(List.of(inputItem)));
            }
        }
        return ret;
    }

    private List<TwinEntity> loadChildren(List<UUID> linkDstTwinIds, UUID linkUuid, UUID ownerBusinessAccountId, Set<UUID> statusIdSet, boolean excludeStatuses) {
        if (statusIdSet == null || statusIdSet.isEmpty()) {
            return twinRepository.findTwinsByHeadOfLinkSrcTowardDstTwins(linkDstTwinIds, linkUuid, ownerBusinessAccountId);
        }
        if (excludeStatuses) {
            return twinRepository.findTwinsByHeadOfLinkSrcTowardDstTwinsStatusesExcluded(
                    linkDstTwinIds, linkUuid, ownerBusinessAccountId, statusIdSet);
        }
        return twinRepository.findTwinsByHeadOfLinkSrcTowardDstTwinsStatusesIncluded(
                linkDstTwinIds, linkUuid, ownerBusinessAccountId, statusIdSet);
    }
}
