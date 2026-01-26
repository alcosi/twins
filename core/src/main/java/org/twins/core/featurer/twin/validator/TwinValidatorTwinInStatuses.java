package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1609,
        name = "Twin in statuses",
        description = "Validator for checking the statuses of a twin")
public class TwinValidatorTwinInStatuses extends TwinValidator {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> statusIdSet = statusIds.extract(properties);
        boolean isValid = statusIdSet.contains(twinEntity.getTwinStatusId());
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no statuses[" + StringUtils.join(statusIdSet, ",") + "]",
                twinEntity.logShort() + " has one of the statuses[" + StringUtils.join(statusIdSet, ",") + "]");
    }

}
