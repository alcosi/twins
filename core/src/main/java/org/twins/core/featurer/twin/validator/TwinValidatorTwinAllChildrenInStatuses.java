package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;


@Slf4j
@Component
@Featurer(id = 1605,
        name = "TransitionValidatorTwinAllChildrenInStatuses",
        description = "")
public class TwinValidatorTwinAllChildrenInStatuses extends TwinValidator {

    @FeaturerParam(name = "childrenTwinClassId", description = "")
    public static final FeaturerParamUUID childrenTwinClassId = new FeaturerParamUUID("childrenTwinClassId");

    @FeaturerParam(name = "childrenTwinStatusId", description = "")
    public static final FeaturerParamUUID childrenTwinStatusId = new FeaturerParamUUID("childrenTwinStatusId");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        UUID classId = childrenTwinClassId.extract(properties);
        UUID statusId = childrenTwinStatusId.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addHeaderTwinId(twinEntity.getId())
                .addTwinClassId(classId, false)
                .addStatusId(statusId, true);
        long count = twinSearchService.count(search);
        boolean isValid = count == 0;

        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " children of class[" + childrenTwinClassId + "] is not in status [" + childrenTwinStatusId + "]",
                twinEntity.logShort() + " all children of class[" + childrenTwinClassId + "] are in status [" + childrenTwinStatusId + "]");
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        UUID classId = childrenTwinClassId.extract(properties);
        UUID statusId = childrenTwinStatusId.extract(properties);
        BasicSearch search = new BasicSearch();
        search.addTwinClassId(classId, false).addStatusId(statusId, true);
        Map<UUID, Long> counts = twinSearchService.countGroupBy(search, TwinEntity.Fields.headTwinId);
        CollectionValidationResult result = new CollectionValidationResult();
        for (TwinEntity twinEntity : twinEntityCollection) {
            long count = counts.getOrDefault(twinEntity.getId(), 0L);
            boolean isValid = count == 0;
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " children of class[" + classId + "] are not in status [" + statusId + "]",
                    twinEntity.logShort() + " all children of class[" + classId + "] are in status [" + statusId + "]"
            ));
        }
        return result;
    }

}
