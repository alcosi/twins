package org.twins.core.featurer.classfield.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionExclusionEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.projection.ProjectionExclusionService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_3208,
        name = "Field finder by id exclude projection",
        description = "")
public class FieldFinderByIdExcludeProjection extends FieldFinder {
    private final ProjectionExclusionService projectionExclusionServiceService;

    @FeaturerParam(name = "Field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        List<ProjectionExclusionEntity> projectionExclusionEntities = projectionExclusionServiceService.findByClassFieldIdSet(fieldIds.extract(properties));

        Set<UUID> fieldIdSet = new HashSet<>();
        for (ProjectionExclusionEntity projectionExclusionEntity : projectionExclusionEntities) {
            fieldIdSet.add(projectionExclusionEntity.getTwinClassFieldId());
        }

        fieldSearch.setIdList(fieldIdSet);
    }
}
