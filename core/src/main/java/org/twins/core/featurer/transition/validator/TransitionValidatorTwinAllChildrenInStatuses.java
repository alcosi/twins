package org.twins.core.featurer.transition.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 1605,
        name = "TransitionValidatorTwinAllChildrenInStatuses",
        description = "")
public class TransitionValidatorTwinAllChildrenInStatuses extends TransitionValidator {

    @FeaturerParam(name = "childrenTwinClassId", description = "")
    public static final FeaturerParamUUIDSet childrenTwinClassId = new FeaturerParamUUIDSet("childrenTwinClassId");

    @FeaturerParam(name = "childrenTwinStatusId", description = "")
    public static final FeaturerParamUUIDSet childrenTwinStatusId = new FeaturerParamUUIDSet("childrenTwinStatusId");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity) throws ServiceException {
        Set<UUID> classIdSet = childrenTwinClassId.extract(properties);
        Set<UUID> statusId = childrenTwinStatusId.extract(properties);

        boolean isValid = true;
        StringBuilder validationMessage = new StringBuilder();

        BasicSearch searchBy = new BasicSearch()
                .addHeaderTwinId(twinEntity.getId())
                .addTwinClassId(classIdSet);

        List<TwinEntity> childrenTwins = twinSearchService.findTwins(searchBy);

        // no children to validate
        if (CollectionUtils.isEmpty(childrenTwins)) {
            return new ValidationResult().setValid(true);
        }

        // validate each child
        for (TwinEntity childTwin : childrenTwins) {
            long count = twinSearchService.count(new BasicSearch()
                    .addHeaderTwinId(twinEntity.getId())
                    .addTwinId(childTwin.getId())
                    .addStatusId(statusId)
            );

            if (count <= 0) {
                isValid = false;
                validationMessage.append(String.format(" [Twin children %s]", childTwin.logShort()));
            }
        }

        return new ValidationResult()
                .setValid(isValid)
                .setMessage(isValid ? "" : twinEntity.logShort() + validationMessage + " is not in statuses[" + StringUtils.join(statusId, ",") + "]");
    }

}
