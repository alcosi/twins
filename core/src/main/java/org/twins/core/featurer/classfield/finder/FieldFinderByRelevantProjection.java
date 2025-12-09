package org.twins.core.featurer.classfield.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.FieldProjectionSearch;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.projection.ProjectionFieldSelector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetProjectionTypeGroupId;
import org.twins.core.service.projection.ProjectionTypeService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
@Featurer(id = FeaturerTwins.ID_3209,
        name = "Fields by relevant mode",
        description = "")
public class FieldFinderByRelevantProjection extends FieldFinder {
    @FeaturerParam(name = "Projection type group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet projectionTypeGroupIds = new FeaturerParamUUIDSetProjectionTypeGroupId("projectionTypeGroupIds");

    private final ProjectionTypeService projectionTypeService;
    private final TwinSearchService twinSearchService;

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        Set<UUID> groupIds = projectionTypeGroupIds.extract(properties);

        KitGrouped<ProjectionTypeEntity, UUID, UUID> groupedProjections = projectionTypeService.findAndGroupByTwinClassId(groupIds);

        if (groupedProjections.isEmpty()) {
            return;
        }

        Set<UUID> twinClassIds = groupedProjections.getGroupedKeySet();

        BasicSearch basicSearch = new BasicSearch();
        basicSearch.setTwinClassIdList(twinClassIds);
        List<TwinEntity> twins = twinSearchService.findTwins(basicSearch);

        Set<UUID> relevantProjectionIds = twins.stream()
                .map(TwinEntity::getTwinClassId)
                .filter(groupedProjections::containsGroupedKey)
                .flatMap(classId -> groupedProjections.getGrouped(classId).stream())
                .map(ProjectionTypeEntity::getId)
                .collect(Collectors.toSet());

        fieldSearch.setFieldProjectionSearch(new FieldProjectionSearch()
                .setProjectionFieldSelector(ProjectionFieldSelector.src)
                .setProjectionTypeIdList(relevantProjectionIds));
    }
}
