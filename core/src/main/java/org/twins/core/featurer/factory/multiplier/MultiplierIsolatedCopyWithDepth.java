package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(
        id = FeaturerTwins.ID_2212,
        name = "Isolated copy with depth",
        description = "New output twin with children for each input. Output class will be taken from input twin."
)
@Slf4j
public class MultiplierIsolatedCopyWithDepth extends Multiplier {

    @FeaturerParam(name = "Children Depth", description = "Level of depth in twin hierarchy tree", defaultValue = "0")
    public static final FeaturerParamInt childrenDepth =  new FeaturerParamInt("childrenDepth");

    @FeaturerParam(name = "Children statuses", description = "Statuses that are used to filter twin children", optional = true)
    public static final FeaturerParamUUIDSetTwinsStatusId childrenStatuses = new FeaturerParamUUIDSetTwinsStatusId("childrenStatuses");

    @Lazy
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        var user = authService.getApiUser().getUser();
        var ret = new ArrayList<FactoryItem>();
        var childrenStatusIds = childrenStatuses.extract(properties);
        var depth = childrenDepth.extract(properties) == -1 ? Integer.MAX_VALUE : childrenDepth.extract(properties);

        // parent twins
        var twinsToCopy = inputFactoryItemList.stream()
                .map(FactoryItem::getTwin)
                .collect(Collectors.toSet());

        // hierarchy paths from parents
        var hierarchyPaths = inputFactoryItemList.stream()
                .map(FactoryItem::getTwin)
                .map(TwinEntity::getHierarchyTree)
                .collect(Collectors.toSet());

        var search = new BasicSearch().setCheckViewPermission(false);
        if (!childrenStatusIds.isEmpty()) {
            search.addStatusId(childrenStatusIds, false);
        }
        search
                .setHierarchyPaths(hierarchyPaths)
                .setMaxChildrenDepth(depth);

        // using set to remove duplicate twins from db search
        var twinsChildren = twinSearchService.findTwinsSet(search);
        twinsToCopy.addAll(twinsChildren);

        // sort to have confidence that twin on every depth level in processing has an already created parent
        var twinsToCopySorted = twinsToCopy.stream()
                .sorted((t1, t2) -> {
                    var h1 = t1.getHierarchyTree().split("\\.").length;
                    var h2 = t2.getHierarchyTree().split("\\.").length;
                    return Integer.compare(h1, h2);
                })
                .toList();

        var oldToNewTwinMap = new HashMap<UUID, TwinEntity>();

        for (var oldTwin : twinsToCopySorted) {
            var newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(oldTwin.getTwinClass())
                    .setTwinClassId(oldTwin.getTwinClassId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(user.getId())
                    .setCreatedByUser(user);

            if (oldTwin.getHeadTwinId() != null) {
                var newHeadTwin = oldToNewTwinMap.get(oldTwin.getHeadTwinId());
                newTwin
                        .setHeadTwin(newHeadTwin)
                        .setHeadTwinId(newHeadTwin.getId());
            }

            oldToNewTwinMap.put(oldTwin.getId(), newTwin);
        }

        for (var inputItem : inputFactoryItemList) {
            var newTwin = oldToNewTwinMap.get(inputItem.getTwin().getId());
            var twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(inputItem))
            );
        }

        return ret;
    }
}
