package org.twins.core.featurer.transition.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 1605,
        name = "TransitionValidatorTwinAllChildrenInStatuses",
        description = "")
public class TransitionValidatorTwinAllChildrenInStatuses extends TransitionValidator {

    @FeaturerParam(name = "childrenTwinClassId", description = "")
    public static final FeaturerParamUUID childrenTwinClassId = new FeaturerParamUUID("childrenTwinClassId");

    @FeaturerParam(name = "childrenTwinStatusId", description = "")
    public static final FeaturerParamUUID childrenTwinStatusId = new FeaturerParamUUID("childrenTwinStatusId");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity) throws ServiceException {
        UUID classId = childrenTwinClassId.extract(properties);
        UUID statusId = childrenTwinStatusId.extract(properties);

        long count = twinSearchService.count(new BasicSearch()
                .addHeaderTwinId(twinEntity.getId())
                .addTwinClassId(classId)
                .addStatusIdExclude(statusId)
        );

        boolean isValid = count == 0;

        return new ValidationResult()
                .setValid(isValid)
                .setMessage(isValid ? "" : twinEntity.logShort() + " children [" + childrenTwinStatusId  + "] is not in status [" + childrenTwinStatusId + "]");
    }

}
