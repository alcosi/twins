package org.twins.core.featurer.twin.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1604,
        name = "Twin has children in statuses",
        description = "")
@RequiredArgsConstructor
public class TwinValidatorTwinHasChildrenInStatuses extends TwinValidator {
    @FeaturerParam(name = "Status ids", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Lazy
    private final TwinSearchService twinSearchService;
    @Lazy
    private final TwinService twinService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> statusIdSet = statusIds.extract(properties);
        BasicSearch search = new BasicSearch();
        search.addHeadTwinId(twinEntity.getId());

        // Load all children to check their statuses (taking freeze into account)
        List<TwinEntity> children = twinSearchService.findTwins(search);

        long count = children.stream()
                .filter(child -> statusIdSet.contains(twinService.getTwinStatusId(child)))
                .count();

        boolean isValid = count > 0;
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no children in statuses[" + StringUtils.join(statusIdSet, ",") + "]",
                twinEntity.logShort() + " has " + count + " children in statuses[" + StringUtils.join(statusIdSet, ",") + "]");
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Set<UUID> statusIdSet = statusIds.extract(properties);
        Set<UUID> parentIds = twinEntityCollection.stream().map(TwinEntity::getId).collect(Collectors.toSet());

        BasicSearch search = new BasicSearch();
        search.setHeadTwinIdList(parentIds);

        // Load all children for all parents
        List<TwinEntity> allChildren = twinSearchService.findTwins(search);

        // Group children by head twin id
        Map<UUID, List<TwinEntity>> childrenByParent = allChildren.stream()
                .collect(Collectors.groupingBy(TwinEntity::getHeadTwinId));

        CollectionValidationResult result = new CollectionValidationResult();
        for (TwinEntity twinEntity : twinEntityCollection) {
            List<TwinEntity> children = childrenByParent.getOrDefault(twinEntity.getId(), Collections.emptyList());
            long count = children.stream()
                    .filter(child -> statusIdSet.contains(twinService.getTwinStatusId(child)))
                    .count();
            boolean isValid = count > 0;
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no children in statuses[" + StringUtils.join(statusIdSet, ",") + "]",
                    twinEntity.logShort() + " has " + count + " children in statuses[" + StringUtils.join(statusIdSet, ",") + "]"
            ));
        }
        return result;
    }

}
