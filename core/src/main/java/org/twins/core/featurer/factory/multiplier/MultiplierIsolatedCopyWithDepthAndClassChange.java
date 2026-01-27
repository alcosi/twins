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

    @FeaturerParam(name = "Link replace map", description = "Map of old link ID to new link ID.")
    public static final FeaturerParamMap linksReplaceMap = new FeaturerParamMap("linksReplaceMap");

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

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var classReplaceMap = twinClassReplaceMap.extract(properties);
        var linkReplaceMap = linksReplaceMap.extract(properties);

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

        Set<TwinLinkEntity> origTwinLinks;
        if (!childrenStatusIds.isEmpty()) {
            search.addStatusId(childrenStatusIds, false);
            origTwinLinks = twinLinkService.findAllWithinHierarchiesAndTwinsInStatusIds(copyContextMap.keySet(), childrenStatusIds);
        } else {
            origTwinLinks = twinLinkService.findAllWithinHierarchies(copyContextMap.keySet());
        }

        var newLinkIds = linkReplaceMap.values().stream().map(UUID::fromString).toList();
        var newLinks = new Kit<>(linkService.findAllByIdIn(newLinkIds), LinkEntity::getId);
        var origTwinLinksGrouped = new KitGrouped<>(origTwinLinks, TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);
        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);

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
            // Skip twins whose class is not in the replace map
            if (!classReplaceMap.containsKey(origTwin.getTwinClassId().toString())) {
                continue;
            }

            var ctx = createCopyContext(origTwin, user, copyContextMap, properties);

            if (origTwinLinksGrouped.containsGroupedKey(origTwin.getId())) {
                ctx.setLinksCopy(
                        copyForwardLinks(ctx.getTwinCopy(), origTwinLinksGrouped.getGrouped(origTwin.getId()), user, copyContextMap, newLinks, properties)
                );
            }
        }

        var ret = new ArrayList<FactoryItem>(copyContextMap.size());
        for (var ctx : copyContextMap.values()) {
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

    private CopyContext createCopyContext(TwinEntity origTwin, UserEntity user, Map<UUID, CopyContext> copyContextMap, Properties properties) throws ServiceException {
        var classReplaceMap = twinClassReplaceMap.extract(properties);

        // get existing context (for input twins) or create a new one (usually for children)
        var copyContext = copyContextMap.computeIfAbsent(
                origTwin.getId(),
                k -> new CopyContext()
                        .setOrigFactoryItem(
                                new FactoryItem()
                                        .setOutput(
                                                new TwinUpdate().setDbTwinEntity(origTwin)
                                        )
                                        .setContextFactoryItemList(
                                                List.of(copyContextMap.get(origTwin.getHeadTwinId()).getOrigFactoryItem())
                                        )
                        )
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

        if (copyContextMap.containsKey(origTwin.getHeadTwinId())) {
            var headTwinCopy = copyContextMap
                    .get(origTwin.getHeadTwinId())
                    .getTwinCopy();

            twinCopy
                    .setHeadTwin(headTwinCopy)
                    .setHeadTwinId(headTwinCopy.getId());
        }

        // setting twin copy in context
        copyContext.setTwinCopy(twinCopy);

        return copyContext;
    }

    private List<TwinLinkEntity> copyForwardLinks(TwinEntity srcTwinCopy, List<TwinLinkEntity> origTwinLinks, UserEntity user, Map<UUID, CopyContext> copyContextMap, Kit<LinkEntity, UUID> newLinks,  Properties properties) throws ServiceException {
        var linkReplaceMap = linksReplaceMap.extract(properties);
        var linksCopy = new ArrayList<TwinLinkEntity>(origTwinLinks.size());

        for (var origTwinLink : origTwinLinks) {
            if (!copyContextMap.containsKey(origTwinLink.getDstTwinId())) {
                continue;
            }

            var dstCtx = createCopyContext(origTwinLink.getDstTwin(), user, copyContextMap, properties);
            var dstTwinCopy = dstCtx.getTwinCopy();
            var newLinkId = UUID.fromString(linkReplaceMap.get(origTwinLink.getLinkId().toString()));

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
}