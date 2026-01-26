package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1611,
        name = "Twin has children of classes",
        description = "")
public class TwinValidatorTwinHasChildrenOfClasses extends TwinValidator {
    @FeaturerParam(name = "Class ids", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet classIds = new FeaturerParamUUIDSetTwinsClassId("classIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> classIdSet = classIds.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addHeadTwinId(twinEntity.getId())
                .addTwinClassExtendsHierarchyContainsId(classIdSet);
        long count = twinSearchService.count(search);
        boolean isValid = count > 0;
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no children of classes[" + StringUtils.join(classIdSet, ",") + "]",
                twinEntity.logShort() + " has " + count + " children of classes[" + StringUtils.join(classIdSet, ",") + "]");
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Set<UUID> classIdSet = classIds.extract(properties);
        BasicSearch search = new BasicSearch();
        search.addTwinClassExtendsHierarchyContainsId(classIdSet);
        Map<UUID, Long> counts = twinSearchService.countGroupBy(search, TwinEntity.Fields.headTwinId);
        CollectionValidationResult result = new CollectionValidationResult();
        for (TwinEntity twinEntity : twinEntityCollection) {
            long count = counts.getOrDefault(twinEntity.getId(), 0L);
            boolean isValid = count > 0;
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no children of classes[" + StringUtils.join(classIdSet, ",") + "]",
                    twinEntity.logShort() + " has " + count + " children of classes[" + StringUtils.join(classIdSet, ",") + "]"
            ));
        }
        return result;
    }

}
