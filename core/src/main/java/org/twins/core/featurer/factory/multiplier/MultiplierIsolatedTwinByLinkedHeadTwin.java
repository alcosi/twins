package org.twins.core.featurer.factory.multiplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.kit.KitGrouped;
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
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves “head” twins via a forward link to the factory input, then loads their twins (by {@code headTwinId})
 * with optional status filtering. Selection logic matches {@link TwinRepository#updateTwinStatusByLinkAndHeadTwinChildren}
 * but returns entities instead of updating.
 */
@Slf4j
@Component
@Featurer(
        id = FeaturerTwins.ID_2214,
        name = "Isolated twin by linked head twin",
        description = "For each input twin: via link (src→dst=input) collect src twins as heads, then output twins whose "
                + "headTwinId is one of those heads, filtered by statusIds (JPQL in TwinRepository)."
)
@RequiredArgsConstructor
public class MultiplierIsolatedTwinByLinkedHeadTwin extends Multiplier {

    @FeaturerParam(name = "Link id", description = "Forward link: src twin is head candidate, dst twin is factory input", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Status ids", description = "Statuses of twins (by head). If empty — any status", order = 2)
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

        List<UUID> inputTwinIds = inputFactoryItemList.stream()
                .map(factoryItem -> factoryItem.getTwin().getId())
                .toList();
        KitGrouped<TwinRepository.DstToSrcHeadProjection, String, UUID> dstToHeadLinksKit = loadDstToHeadLinks(inputTwinIds, uuidLink);
        Set<UUID> allHeadTwinIds = dstToHeadLinksKit.getCollection().stream()
                .map(TwinRepository.DstToSrcHeadProjection::getSrcTwinId)
                .collect(Collectors.toSet());
        KitGrouped<TwinEntity, UUID, UUID> twinsByHeadTwinId = new KitGrouped<>(
                loadTwinsByHeads(allHeadTwinIds, ownerBusinessAccountId, statusIdSet, exclude),
                TwinEntity::getId,
                TwinEntity::getHeadTwinId
        );

        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            List<TwinRepository.DstToSrcHeadProjection> linksForInputTwin = dstToHeadLinksKit.getGrouped(inputTwin.getId());
            if (CollectionUtils.isEmpty(linksForInputTwin)) {
                log.warn("{} no linked heads for input {}", inputTwin.logShort(), inputTwin.getId());
                continue;
            }
            List<TwinEntity> linkedTwins = linksForInputTwin.stream()
                    .map(TwinRepository.DstToSrcHeadProjection::getSrcTwinId)
                    .map(twinsByHeadTwinId::getGrouped)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .toList();
            if (CollectionUtils.isEmpty(linkedTwins)) {
                log.warn("{} no twins for heads linked to input {}", inputTwin.logShort(), inputTwin.getId());
                continue;
            }
            for (TwinEntity twin : linkedTwins) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate
                        .setDbTwinEntity(twin)
                        .setTwinEntity(twin.clone());
                ret.add(new FactoryItem()
                        .setOutput(twinUpdate)
                        .setContextFactoryItemList(List.of(inputItem)));
            }
        }
        return ret;
    }

    private KitGrouped<TwinRepository.DstToSrcHeadProjection, String, UUID> loadDstToHeadLinks(Collection<UUID> linkDstTwinIds, UUID linkUuid) {
        return new KitGrouped<>(
                twinRepository.findDstToSrcHeadsByDstTwinIdsAndLinkId(linkDstTwinIds, linkUuid),
                row -> row.getDstTwinId().toString() + ":" + row.getSrcTwinId().toString(),
                TwinRepository.DstToSrcHeadProjection::getDstTwinId
        );
    }

    private List<TwinEntity> loadTwinsByHeads(Set<UUID> headTwinIds, UUID ownerBusinessAccountId, Set<UUID> statusIdSet, boolean excludeStatuses) {
        if (CollectionUtils.isEmpty(headTwinIds)) {
            return List.of();
        }
        if (statusIdSet == null || statusIdSet.isEmpty()) {
            return twinRepository.findByOwnerBusinessAccountIdAndHeadTwinIdIn(ownerBusinessAccountId, headTwinIds);
        } else if (excludeStatuses) {
            return twinRepository.findByOwnerBusinessAccountIdAndHeadTwinIdInAndTwinStatusIdNotIn(ownerBusinessAccountId, headTwinIds, statusIdSet);
        } else {
            return twinRepository.findByOwnerBusinessAccountIdAndHeadTwinIdInAndTwinStatusIdIn(ownerBusinessAccountId, headTwinIds, statusIdSet);
        }
    }
}
