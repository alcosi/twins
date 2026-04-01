package org.twins.core.featurer.factory.multiplier;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
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
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinSearchService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(
        id = FeaturerTwins.ID_2213,
        name = "Isolated copy with depth and class change",
        description = "New output twin with children for each input. Output class will be taken from twinClassReplaceMap."
)
@Slf4j
@RequiredArgsConstructor
public class MultiplierIsolatedCopyWithDepthAndClassChange extends Multiplier {

    @FeaturerParam(name = "Children Depth", description = "Level of depth in twin hierarchy tree", optional = true, defaultValue = "0")
    public static final FeaturerParamInt childrenDepth = new FeaturerParamInt("childrenDepth");

    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    @FeaturerParam(name = "Twin class replace map", description = "Map of old twin class ID to new twin class ID. Twins with classes not in map keys will be skipped.")
    public static final FeaturerParamMap twinClassReplaceMap = new FeaturerParamMap("twinClassReplaceMap");

    @FeaturerParam(name = "Link replace map", description = "Map of old link ID to new link ID.", optional = true)
    public static final FeaturerParamMap linksReplaceMap = new FeaturerParamMap("linksReplaceMap");

    @FeaturerParam(name = "Linked twins by link id", description = "Map of link id and boolean param (Ture -> search twins as src. False -> search twins as dst)", optional = true)
    public static final FeaturerParamMap linkedTwinsByLinkIdMap = new FeaturerParamMap("linkedTwinsByLinkIdMap");

    private final TwinSearchService twinSearchService;
    private final TwinLinkService twinLinkService;
    private final LinkService linkService;

    @Data
    @Accessors(chain = true)
    private static class CopyContext {
        private TwinEntity twinCopy;
        private List<TwinLinkEntity> linksCopy;
        private FactoryItem origFactoryItem;
    }

    private record LinksData(Set<TwinLinkEntity> origTwinLinks, Kit<LinkEntity, UUID> newLinks) {
        static final LinksData EMPTY = new LinksData(Collections.emptySet(), new Kit<>(LinkEntity::getId));
    }

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var classReplaceMap = twinClassReplaceMap.extract(properties);
        var linkReplaceMap = extractLinkReplaceMap(properties);

        var copyContextMap = new LinkedHashMap<UUID, CopyContext>();
        var copyTwinIdsMap = new HashMap<UUID, UUID>();
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

        var linksData = findLinksData(copyContextMap.keySet(), linkReplaceMap, childrenStatusIds);
        var origTwinLinksGrouped = new KitGrouped<>(linksData.origTwinLinks(), TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);
        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);
        origTwins.addAll(findLinkedTwins(origTwins, extractLinkedTwinsByLinkIdMap(properties), childrenStatusIds));
        var twinsToCopyIds = origTwins.stream()
                .filter(twin -> classReplaceMap.containsKey(twin.getTwinClassId().toString()))
                .map(TwinEntity::getId)
                .collect(Collectors.toSet());

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

                    return Boolean.compare(t1HasLinks, t2HasLinks);
                })
                .toList();

        for (var origTwin : origTwinsSorted) {
            // Skip twins which are not selected for copy
            if (!twinsToCopyIds.contains(origTwin.getId())) {
                continue;
            }

            var ctx = createCopyContext(origTwin, user, copyContextMap, copyTwinIdsMap, properties);

            if (origTwinLinksGrouped.containsGroupedKey(origTwin.getId())) {
                ctx.setLinksCopy(
                        copyForwardLinks(ctx.getTwinCopy(), origTwinLinksGrouped.getGrouped(origTwin.getId()), user, copyContextMap, copyTwinIdsMap, twinsToCopyIds, linksData.newLinks(), properties)
                );
            }
        }

        var ret = new ArrayList<FactoryItem>(copyContextMap.size());
        for (var ctx : copyContextMap.values()) {
            if (ctx.getTwinCopy() == null) {
                continue;
            }
            var twinCreate = new TwinCreate();
            twinCreate
                    .setLinksEntityList(ctx.getLinksCopy())
                    .setTwinEntity(ctx.getTwinCopy());
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(ctx.getOrigFactoryItem()))
            );
        }

        return ret;
    }

    private CopyContext createCopyContext(TwinEntity origTwin, UserEntity user, Map<UUID, CopyContext> copyContextMap, Map<UUID, UUID> copyTwinIdsMap, Properties properties) throws ServiceException {
        var classReplaceMap = twinClassReplaceMap.extract(properties);

        // get existing context (for input twins) or create a new one (usually for children)
        var copyContext = copyContextMap.computeIfAbsent(
                origTwin.getId(),
                k -> {
                    var parentContext = copyContextMap.get(origTwin.getHeadTwinId());
                    var parentContextItems = parentContext != null
                            ? List.of(parentContext.getOrigFactoryItem())
                            : Collections.<FactoryItem>emptyList();
                    return new CopyContext()
                            .setOrigFactoryItem(
                                    new FactoryItem()
                                            .setOutput(
                                                    new TwinUpdate().setDbTwinEntity(origTwin)
                                            )
                                            .setContextFactoryItemList(parentContextItems)
                            );
                }
        );

        if (copyContext.getTwinCopy() != null) {
            // already created context
            return copyContext;
        }

        var newClassId = UUID.fromString(classReplaceMap.get(origTwin.getTwinClassId().toString()));
        var newClass = twinClassService.findEntitySafe(newClassId);

        // creating twin copy with head copy and new class
        var twinCopy = new TwinEntity()
                .setId(UuidUtils.generate())
                .setName("")
                .setTwinClass(newClass)
                .setTwinClassId(newClassId)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(user.getId())
                .setCreatedByUser(user);

        var origHeadTwinId = origTwin.getHeadTwinId();
        if (origHeadTwinId != null) {
            if (copyTwinIdsMap.containsKey(origHeadTwinId)) {
                twinCopy.setHeadTwinId(copyTwinIdsMap.get(origHeadTwinId));
                var headContext = copyContextMap.get(origHeadTwinId);
                if (headContext != null && headContext.getTwinCopy() != null) {
                    twinCopy.setHeadTwin(headContext.getTwinCopy());
                }
            } else {
                // keep original head when head twin is not part of copy scope
                twinCopy.setHeadTwinId(origHeadTwinId);
            }
        }

        // setting twin copy in context
        copyContext.setTwinCopy(twinCopy);
        copyTwinIdsMap.put(origTwin.getId(), twinCopy.getId());

        return copyContext;
    }

    private List<TwinLinkEntity> copyForwardLinks(TwinEntity srcTwinCopy, List<TwinLinkEntity> origTwinLinks, UserEntity user, Map<UUID, CopyContext> copyContextMap, Map<UUID, UUID> copyTwinIdsMap, Set<UUID> twinsToCopyIds, Kit<LinkEntity, UUID> newLinks,  Properties properties) throws ServiceException {
        var linkReplaceMap = extractLinkReplaceMap(properties);
        var linksCopy = new ArrayList<TwinLinkEntity>(origTwinLinks.size());

        for (var origTwinLink : origTwinLinks) {
            if (!twinsToCopyIds.contains(origTwinLink.getDstTwinId())) {
                continue;
            }

            var dstCtx = createCopyContext(origTwinLink.getDstTwin(), user, copyContextMap, copyTwinIdsMap, properties);
            var dstTwinCopy = dstCtx.getTwinCopy();
            var newLinkId = linkReplaceMap.get(origTwinLink.getLinkId());

            var linkCopy = new TwinLinkEntity()
                    .setId(UuidUtils.generate())
                    .setSrcTwinId(srcTwinCopy.getId())
                    .setSrcTwin(srcTwinCopy)
                    .setDstTwinId(dstTwinCopy.getId())
                    .setDstTwin(dstTwinCopy)
                    .setLinkId(newLinkId)
                    .setLink(newLinks.get(newLinkId))
                    .setCreatedByUserId(origTwinLink.getCreatedByUserId())
                    .setCreatedByUser(origTwinLink.getCreatedByUser())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            linksCopy.add(linkCopy);
        }

        return linksCopy;
    }

    private Map<UUID, UUID> extractLinkReplaceMap(Properties properties) {
        var extracted = linksReplaceMap.extract(properties);

        if (extracted.isEmpty()) {
            return Collections.emptyMap();
        }

        return extracted.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> UUID.fromString(entry.getValue())
                ));
    }

    private Map<UUID, Boolean> extractLinkedTwinsByLinkIdMap(Properties properties) {
        var extracted = linkedTwinsByLinkIdMap.extract(properties);

        if (extracted.isEmpty()) {
            return Collections.emptyMap();
        }

        return extracted.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> Boolean.parseBoolean(entry.getValue())
                ));
    }

    private LinksData findLinksData(Set<UUID> hierarchies, Map<UUID, UUID> linkReplaceMap, Set<UUID> childrenStatusIds) throws ServiceException {
        if (linkReplaceMap.isEmpty()) {
            return LinksData.EMPTY;
        }

        var origTwinLinks = childrenStatusIds.isEmpty()
                ? twinLinkService.findAllWithinHierarchiesAndLinkIdIn(hierarchies, linkReplaceMap.keySet())
                : twinLinkService.findAllWithinHierarchiesAndLinkIdInAndTwinsInStatusIds(hierarchies, linkReplaceMap.keySet(), childrenStatusIds);

        var newLinks = linkService.findEntitiesSafe(linkReplaceMap.values());

        return new LinksData(origTwinLinks, newLinks);
    }

    private Set<TwinEntity> findLinkedTwins(Collection<TwinEntity> hierarchyTwins, Map<UUID, Boolean> linkMap, Set<UUID> childrenStatusIds) throws ServiceException {
        if (linkMap == null || hierarchyTwins.isEmpty()) {
            return Collections.emptySet();
        }

        var hierarchyTwinIds = hierarchyTwins.stream()
                .map(TwinEntity::getId)
                .collect(Collectors.toSet());

        var linkedSearch = new BasicSearch().setCheckViewPermission(false);
        for (var link : linkMap.entrySet()) {
            if (link.getValue()) {
                linkedSearch
                        .addLinkSrcTwinsId(link.getKey(), hierarchyTwinIds, false, true);
            } else {
                linkedSearch
                        .addLinkDstTwinsId(link.getKey(), hierarchyTwinIds, false, true);
            }
        }

        if (!childrenStatusIds.isEmpty()) {
            linkedSearch.addStatusId(childrenStatusIds, false);
        }

        var linkedTwins = new HashSet<>(twinSearchService.findTwins(linkedSearch));
        linkedTwins.removeIf(twin -> hierarchyTwinIds.contains(twin.getId()));
        return linkedTwins;
    }
}