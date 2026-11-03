package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinSearchId;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2729,
        name = "By head of subquery twins",
        description = "Keeps twins that are the head twin of twins returned by a saved search.")
public class TwinFinderByHeadOfSubquery extends TwinFinderSubquery {

    @FeaturerParam(name = "Subquery search id",
            description = "Saved search whose twins provide the head twin ids",
            order = 1)
    public static final FeaturerParamUUID subQueryTwinSearchId = new FeaturerParamUUIDTwinsTwinSearchId("subQueryTwinSearchId");

    @FeaturerParam(name = "Subquery twins limit",
            description = "Max subquery twins to resolve; overflow is warned and truncated",
            order = 2, optional = true, defaultValue = "1000")
    public static final FeaturerParamInt subqueryTwinsLimit = new FeaturerParamInt("subqueryTwinsLimit");

    @Autowired
    TwinRepository twinRepository;

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        int limit = subqueryTwinsLimit.extract(properties);
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "subqueryTwinsLimit must be positive");
        Set<UUID> subqueryTwinIds = takeSubqueryTwinIds(subQueryTwinSearchId.extract(properties), namedParamsMap, limit);
        if (CollectionUtils.isEmpty(subqueryTwinIds)) {
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        Set<UUID> headTwinIds = twinRepository.findDistinctHeadTwinIdByIdIn(subqueryTwinIds);
        if (CollectionUtils.isEmpty(headTwinIds)) {
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        twinSearch.addTwinId(headTwinIds, false);
    }
}
