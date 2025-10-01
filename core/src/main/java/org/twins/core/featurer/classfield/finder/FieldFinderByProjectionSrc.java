package org.twins.core.featurer.classfield.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.projection.ProjectionService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_3210,
        name = "Field finder by projection src",
        description = "")
public class FieldFinderByProjectionSrc extends FieldFinder {
    private final ProjectionService projectionService;

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        List<ProjectionEntity> projectionEntityList = projectionService.findAll();

        Set<UUID> fieldIdSet = new HashSet<>();
        for (ProjectionEntity projectionEntity : projectionEntityList) {
            fieldIdSet.add(projectionEntity.getSrcTwinClassFieldId());
        }

        if (exclude.extract(properties)) {
            fieldSearch.setIdExcludeList(fieldIdSet);
        } else {
            fieldSearch.setIdList(fieldIdSet);
        }
    }
}
