package org.twins.core.featurer.factory.multiplier;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

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
    public static final FeaturerParamInt childrenDepth = new FeaturerParamInt("childrenDepth");

    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    @FeaturerParam(name = "Link ids", description = "Link types to include connected twins (non-hierarchical). Connected twins will be copied without their children.", optional = true)
    public static final FeaturerParamUUIDSetTwinsLinkId linkIds = new FeaturerParamUUIDSetTwinsLinkId("linkIds");

    private final TwinSearchService twinSearchService;
    private final TwinLinkService twinLinkService;
    private final TwinService twinService;

    @Data
    @Accessors(chain = true)
    private static class CopyContext {
        private TwinEntity twinCopy;
        private List<TwinLinkEntity> linksCopy;
        private FactoryItem origFactoryItem;
    }

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var linkIdsParam = linkIds.extract(properties);
        var copyContextMap = new LinkedHashMap<UUID, CopyContext>();
        var origTwins = new HashSet<TwinEntity>(inputFactoryItemList.size());

        for (var factoryItem : inputFactoryItemList) {
            var twin = factoryItem.getTwin();
            origTwins.add(twin);
            copyContextMap.put(twin.getId(), new CopyContext().setOrigFactoryItem(factoryItem));
        }

        var search = new BasicSearch().setCheckViewPermission(false);
        search.setHierarchyChildrenSearch(
                new HierarchySearch()
                        .setDepth(depth)
                        .setIdList(copyContextMap.keySet())
        );

        if (!childrenStatusIds.isEmpty()) {
            search.addStatusId(childrenStatusIds, false);
        }

        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);

        Set<UUID> collectedTwinIds = new HashSet<>(origTwins.size());
        for (var t : origTwins) {
            collectedTwinIds.add(t.getId());
        }

        // Find connected twins by specified link types
        if (!linkIdsParam.isEmpty()) {
            // Find links where src OR dst is in collectedTwinIds
            Set<TwinLinkEntity> linkTypeLinks = twinLinkService.findAllByLinkIdInAndSrcTwinIdInOrDstTwinIdIn(linkIdsParam, collectedTwinIds);

            Set<UUID> connectedTwinIds = new HashSet<>();
            for (var link : linkTypeLinks) {
                // Add src if it's not in collectedTwinIds
                if (!collectedTwinIds.contains(link.getSrcTwinId())) {
                    connectedTwinIds.add(link.getSrcTwinId());
                }
                // Add dst if it's not in collectedTwinIds
                if (!collectedTwinIds.contains(link.getDstTwinId())) {
                    connectedTwinIds.add(link.getDstTwinId());
                }
            }

            if (!connectedTwinIds.isEmpty()) {
                var connectedTwins = twinService.findEntitiesSafe(connectedTwinIds);
                origTwins.addAll(connectedTwins.getCollection());
                for (var t : connectedTwins) {
                    collectedTwinIds.add(t.getId());
                }
            }
        }

        Set<TwinLinkEntity> origTwinLinks = childrenStatusIds.isEmpty()
                ? twinLinkService.findAllBetweenTwinsIn(collectedTwinIds)
                : twinLinkService.findAllBetweenTwinsInAndTwinsInStatusIds(collectedTwinIds, childrenStatusIds);

        var origTwinLinksGrouped = new KitGrouped<>(origTwinLinks, TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);

        // Build in-degree map for topological sort (how many links point to each twin)
        Map<UUID, Integer> inDegreeMap = new HashMap<>();
        for (TwinEntity twin : origTwins) {
            inDegreeMap.put(twin.getId(), 0);
        }
        for (TwinLinkEntity link : origTwinLinks) {
            inDegreeMap.merge(link.getDstTwinId(), 1, Integer::sum);
        }

        // sort to have confidence that twin on every depth level in processing has an already created parent
        var origTwinsSorted = origTwins.stream()
                .sorted((t1, t2) -> {
                    var h1 = t1.getHierarchyTree().split("\\.").length;
                    var h2 = t2.getHierarchyTree().split("\\.").length;

                    var depthComparison = Integer.compare(h1, h2);
                    if (depthComparison != 0) {
                        // stop sort if twins are on different levels
                        return depthComparison;
                    }

                    // secondary sort: twins without links go first
                    boolean t1HasLinks = origTwinLinksGrouped.containsGroupedKey(t1.getId());
                    boolean t2HasLinks = origTwinLinksGrouped.containsGroupedKey(t2.getId());
                    int linksComparison = Boolean.compare(t1HasLinks, t2HasLinks);
                    if (linksComparison != 0) {
                        return linksComparison;
                    }

                    // tertiary sort: twins that are referenced by more links go first (topological order)
                    int t1InDegree = inDegreeMap.getOrDefault(t1.getId(), 0);
                    int t2InDegree = inDegreeMap.getOrDefault(t2.getId(), 0);
                    return Integer.compare(t2InDegree, t1InDegree); // reverse order - higher in-degree first
                })
                .toList();

        for (var origTwin : origTwinsSorted) {
            // skipping already copied twins (maybe already copied for twinLink)
            if (copyContextMap.get(origTwin.getId()) != null && copyContextMap.get(origTwin.getId()).getTwinCopy() != null) {
                continue;
            }

            var ctx = createCopyContext(origTwin, user, copyContextMap);

            if (origTwinLinksGrouped.containsGroupedKey(origTwin.getId())) {
                ctx.setLinksCopy(
                        copyForwardLinks(ctx.getTwinCopy(), origTwinLinksGrouped.getGrouped(origTwin.getId()), user, copyContextMap)
                );
            }
        }

        var ret = new ArrayList<FactoryItem>(copyContextMap.size());
        for (var copyContext : copyContextMap.values()) {
            var twinCreate = new TwinCreate();
            twinCreate
                    .setLinksEntityList(copyContext.getLinksCopy())
                    .setTwinEntity(copyContext.getTwinCopy());
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(copyContext.getOrigFactoryItem()))
            );
        }

        return ret;
    }

    private CopyContext createCopyContext(TwinEntity origTwin, UserEntity user, Map<UUID, CopyContext> copyContextMap) {
        // get existing context (for input twins) or create a new one (usually for children)
        var copyContext = copyContextMap.computeIfAbsent(
                origTwin.getId(),
                k -> {
                    var factoryItem = new FactoryItem()
                            .setOutput(new TwinUpdate().setDbTwinEntity(origTwin));

                    // Set context factory item list - use head twin context if available, otherwise use input factory item
                    if (origTwin.getHeadTwinId() != null && copyContextMap.containsKey(origTwin.getHeadTwinId())) {
                        factoryItem.setContextFactoryItemList(
                                List.of(copyContextMap.get(origTwin.getHeadTwinId()).getOrigFactoryItem())
                        );
                    } else {
                        // For top-level twins or when head is not yet processed, use the twin itself as context
                        factoryItem.setContextFactoryItemList(List.of(factoryItem));
                    }

                    return new CopyContext().setOrigFactoryItem(factoryItem);
                }
        );

        if (copyContext.getTwinCopy() != null) {
            // already created context
            return copyContext;
        }

        // creating twin copy with head copy
        var twinCopy = new TwinEntity()
                .setId(UuidUtils.generate())
                .setName("")
                .setTwinClass(origTwin.getTwinClass())
                .setTwinClassId(origTwin.getTwinClassId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(user.getId())
                .setCreatedByUser(user);

        var origHeadTwinId = origTwin.getHeadTwinId();
        if (origHeadTwinId != null) {
            if (copyContextMap.containsKey(origHeadTwinId)) {
                // Parent is being copied — point to the copy
                var headTwinCopy = copyContextMap.get(origHeadTwinId).getTwinCopy();
                twinCopy
                        .setHeadTwin(headTwinCopy)
                        .setHeadTwinId(headTwinCopy.getId());
            } else {
                // Parent is outside the copy scope — keep the original reference
                twinCopy.setHeadTwinId(origHeadTwinId);
            }
        }

        // setting twin copy in context
        copyContext.setTwinCopy(twinCopy);

        return copyContext;
    }

    private List<TwinLinkEntity> copyForwardLinks(TwinEntity srcTwinCopy, List<TwinLinkEntity> origTwinLinks, UserEntity user, Map<UUID, CopyContext> copyContextMap) {
        var linksCopy = new ArrayList<TwinLinkEntity>(origTwinLinks.size());

        for (var origTwinLink : origTwinLinks) {
            var dstCtx = createCopyContext(origTwinLink.getDstTwin(), user, copyContextMap);
            var dstTwinCopy = dstCtx.getTwinCopy();

            var linkCopy = new TwinLinkEntity()
                    .setId(UuidUtils.generate())
                    .setSrcTwinId(srcTwinCopy.getId())
                    .setSrcTwin(srcTwinCopy)
                    .setDstTwinId(dstTwinCopy.getId())
                    .setDstTwin(dstTwinCopy)
                    .setLinkId(origTwinLink.getLinkId())
                    .setLink(origTwinLink.getLink())
                    .setCreatedByUserId(origTwinLink.getCreatedByUserId())
                    .setCreatedByUser(origTwinLink.getCreatedByUser())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            linksCopy.add(linkCopy);
        }

        return linksCopy;
    }
}
