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
    public static final FeaturerParamInt childrenDepth = new FeaturerParamInt("childrenDepth");

    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    private final TwinSearchService twinSearchService;
    private final TwinLinkService twinLinkService;

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

        var origTwinLinksGrouped = new KitGrouped<>(origTwinLinks, TwinLinkEntity::getId, TwinLinkEntity::getSrcTwinId);
        var origTwinsChildren = twinSearchService.findTwins(search);
        origTwins.addAll(origTwinsChildren);

        // sort to have confidence that twin on every depth level in processing has an already created parent
        var origTwinsSroted = origTwins.stream()
                .sorted((t1, t2) -> {
                    var h1 = t1.getHierarchyTree().split("\\.").length;
                    var h2 = t2.getHierarchyTree().split("\\.").length;
                    return Integer.compare(h1, h2);
                })
                .toList();

        for (var origTwin : origTwinsSroted) {
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
        var twinCopy = new TwinEntity()
                .setId(UuidUtils.generate())
                .setName("")
                .setTwinClass(origTwin.getTwinClass())
                .setTwinClassId(origTwin.getTwinClassId())
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
