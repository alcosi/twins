package org.twins.core.featurer.transition.validator;

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
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 1604,
        name = "TransitionValidatorTwinHasChildrenInStatuses",
        description = "")
public class TransitionValidatorTwinHasChildrenInStatuses extends TransitionValidator {
    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSet("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity) throws ServiceException {
        Set<UUID> statusIdSet = statusIds.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addHeaderTwinId(twinEntity.getId())
                .addStatusId(statusIdSet);
        long count = twinSearchService.count(search);
        boolean isValid = count > 0;
        return new ValidationResult()
                .setValid(isValid)
                .setMessage(isValid ? "" : twinEntity.logShort() + " has no children in statuses[" + StringUtils.join(statusIdSet, ",") + "]");
    }
}
