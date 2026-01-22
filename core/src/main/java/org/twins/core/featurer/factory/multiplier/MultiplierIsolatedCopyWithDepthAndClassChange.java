package org.twins.core.featurer.factory.multiplier;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
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

    private final TwinSearchService twinSearchService;

    @Data
    @Accessors(chain = true)
    private static class CopyContext {
        private TwinEntity twinCopy;
        private FactoryItem origFactoryItem;
    }

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties);
        var classReplaceMap = twinClassReplaceMap.extract(properties);

        var copyContextMap = new HashMap<UUID, CopyContext>();
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

        // sort to have confidence that twin on every depth level in processing has an already created parent
        var origTwinsSorted = origTwins.stream()
                .sorted((t1, t2) -> {
                    var h1 = t1.getHierarchyTree().split("\\.").length;
                    var h2 = t2.getHierarchyTree().split("\\.").length;
                    return Integer.compare(h1, h2);
                })
                .toList();

        for (var origTwin : origTwinsSorted) {
            // Skip twins whose class is not in the replace map
            if (!classReplaceMap.containsKey(origTwin.getTwinClassId().toString())) {
                continue;
            }

            createCopyContext(origTwin, user, copyContextMap, classReplaceMap);
        }

        var ret = new ArrayList<FactoryItem>(copyContextMap.size());
        for (var ctx : copyContextMap.values()) {
            var twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(ctx.getTwinCopy());
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(ctx.getOrigFactoryItem()))
            );
        }

        return ret;
    }

    private void createCopyContext(TwinEntity origTwin, UserEntity user, Map<UUID, CopyContext> copyContextMap, Map<String, String> classReplaceMap) throws ServiceException {
        var newClassId = UUID.fromString(classReplaceMap.get(origTwin.getTwinClassId().toString()));
        var newClass = twinClassService.findEntitySafe(newClassId);

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
    }
}