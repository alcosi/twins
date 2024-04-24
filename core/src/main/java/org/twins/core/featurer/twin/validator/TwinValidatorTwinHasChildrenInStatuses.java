package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 1604,
        name = "TransitionValidatorTwinHasChildrenInStatuses",
        description = "")
public class TwinValidatorTwinHasChildrenInStatuses extends TwinValidator {
    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSet("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> statusIdSet = statusIds.extract(properties);
        long count = twinSearchService.count(new BasicSearch()
                .addHeaderTwinId(twinEntity.getId())
                .addStatusId(statusIdSet));
        boolean isValid = count > 0;
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no children in statuses[" + StringUtils.join(statusIdSet, ",") + "]",
                twinEntity.logShort() + " has " + count + " children in statuses[" + StringUtils.join(statusIdSet, ",") + "]");
    }
}
