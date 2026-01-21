package org.twins.core.featurer.factory.multiplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
@Featurer(
        id = FeaturerTwins.ID_2212,
        name = "Isolated copy with depth",
        description = "New output twin with children for each input. Output class will be taken from input twin."
)
@Slf4j
@RequiredArgsConstructor
public class MultiplierIsolatedCopyWithDepth extends Multiplier {

    @FeaturerParam(name = "Children Depth", description = "Level of depth in twin hierarchy tree", optional = true, defaultValue = "0")
    public static final FeaturerParamInt childrenDepth =  new FeaturerParamInt("childrenDepth");

    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    private final TwinSearchService twinSearchService;
    private final TwinLinkService twinLinkService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var origTwinIdToFactoryItemMap = new HashMap<UUID, FactoryItem>();
        var origTwins = new HashSet<TwinEntity>(inputFactoryItemList.size());
        Set<TwinLinkEntity> origTwinLinks;

        for (var factoryItem : inputFactoryItemList) {
            var twin = factoryItem.getTwin();
            origTwins.add(twin);
            origTwinIdToFactoryItemMap.put(twin.getId(), factoryItem);
        }

        var search = new BasicSearch().setCheckViewPermission(false);
        search
                .setHierarchyChildrenSearch(
                        new HierarchySearch()
                                .setDepth(depth)
                                .setIdList(origTwinIdToFactoryItemMap.keySet())
                );

        if (!childrenStatusIds.isEmpty()) {
            search.addStatusId(childrenStatusIds, false);
            origTwinLinks = twinLinkService.findAllWithinHierarchiesAndTwinsInStatusIds(origTwinIdToFactoryItemMap.keySet(), childrenStatusIds);
        } else {
            origTwinLinks = twinLinkService.findAllWithinHierarchies(origTwinIdToFactoryItemMap.keySet());
        }

        var origTwinLinksGrouped = new KitGrouped<>(origTwinLinks, TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);
        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);

        // sort to have confidence that twin on every depth level in processing has an already created parent
        var twinsToCopySorted = origTwins.stream()
                .sorted((t1, t2) -> {
                    var h1 = t1.getHierarchyTree().split("\\.").length;
                    var h2 = t2.getHierarchyTree().split("\\.").length;
                    return Integer.compare(h1, h2);
                })
                .toList();

        var origTwinIdToTwinCopyMap = new HashMap<UUID, TwinEntity>();

        for (var origTwin : twinsToCopySorted) {
            // skipping already copied twins (maybe already copied for twinLink)
            if (origTwinIdToTwinCopyMap.containsKey(origTwin.getId())) {
                continue;
            }

            var twinCopy = copyTwin(origTwin, user, origTwinIdToTwinCopyMap, origTwinIdToFactoryItemMap);

            if (origTwinLinksGrouped.containsGroupedKey(origTwin.getId())) {
                twinCopy.setTwinLinks(
                        new TwinLinkService.FindTwinLinksResult()
                                .setForwardLinks(
                                        copyForwardLinks(
                                                twinCopy, origTwinLinksGrouped.getGrouped(origTwin.getId()),
                                                user, origTwinIdToTwinCopyMap, origTwinIdToFactoryItemMap
                                        )
                                )
                                .setTwinId(twinCopy.getId())
                );
            }

            origTwinIdToTwinCopyMap.put(origTwin.getId(), twinCopy);
        }

        var ret = new ArrayList<FactoryItem>(origTwinIdToTwinCopyMap.size());
        for (var entry : origTwinIdToTwinCopyMap.entrySet()) {
            var twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(entry.getValue());
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(origTwinIdToFactoryItemMap.get(entry.getKey())))
            );
        }

        return ret;
    }

    private TwinEntity copyTwin(TwinEntity origTwin, UserEntity user, Map<UUID, TwinEntity> origTwinIdToTwinCopyMap, Map<UUID, FactoryItem> origTwinIdToFactoryItemMap) {
        var twinCopy = new TwinEntity()
                .setId(UuidUtils.generate())
                .setName("")
                .setTwinClass(origTwin.getTwinClass())
                .setTwinClassId(origTwin.getTwinClassId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(user.getId())
                .setCreatedByUser(user);

        if (origTwin.getHeadTwinId() != null && origTwinIdToTwinCopyMap.containsKey(origTwin.getHeadTwinId())) {
            var headTwinCopy = origTwinIdToTwinCopyMap.get(origTwin.getHeadTwinId());
            twinCopy
                    .setHeadTwin(headTwinCopy)
                    .setHeadTwinId(headTwinCopy.getId());

            origTwinIdToFactoryItemMap.put(origTwin.getId(), origTwinIdToFactoryItemMap.get(origTwin.getHeadTwinId()));
        }

        return twinCopy;
    }

    private KitGrouped<TwinLinkEntity, UUID, UUID> copyForwardLinks(TwinEntity copyTwin, List<TwinLinkEntity> origTwinLinks, UserEntity user, Map<UUID, TwinEntity> origTwinIdToTwinCopyMap, Map<UUID, FactoryItem> origTwinIdToFactoryItemMap) {
        var forwardLinksCopy = new KitGrouped<>(TwinLinkEntity::getId, TwinLinkEntity::getLinkId);

        for (var origTwinLink : origTwinLinks) {
            var copyDstTwin = copyTwin(origTwinLink.getDstTwin(), user, origTwinIdToTwinCopyMap, origTwinIdToFactoryItemMap);

            var copyTwinLink = new TwinLinkEntity()
                    .setId(UuidUtils.generate())
                    .setSrcTwinId(copyTwin.getId())
                    .setSrcTwin(copyTwin)
                    .setDstTwinId(copyDstTwin.getId())
                    .setDstTwin(copyDstTwin)
                    .setLinkId(origTwinLink.getLinkId())
                    .setLink(origTwinLink.getLink())
                    .setCreatedByUserId(origTwinLink.getCreatedByUserId())
                    .setCreatedByUser(origTwinLink.getCreatedByUser())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            forwardLinksCopy.add(copyTwinLink);
            origTwinIdToTwinCopyMap.put(origTwinLink.getDstTwinId(), copyDstTwin);
        }

        return forwardLinksCopy;
    }
}
