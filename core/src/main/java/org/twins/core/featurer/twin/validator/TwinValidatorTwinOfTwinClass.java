package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1613,
        name = "Twin of twin classes exists",
        description = "")
public class TwinValidatorTwinOfTwinClass extends TwinValidator {
    @FeaturerParam(name = "Class ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet classIds = new FeaturerParamUUIDSetTwinsClassId("classIds");

    @FeaturerParam(name = "Status ids", description = "", order = 2, optional = true)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> classIdSet = classIds.extract(properties);
        Set<UUID> statusIdSet = statusIds.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(classIdSet, false)
                .addStatusId(statusIdSet, false);

        long count = twinSearchService.count(search);

        return buildResult(
                count > 0,
                invert,
                "there are no twins of given twin class",
                "twins of given twin class exist");
    }
}
