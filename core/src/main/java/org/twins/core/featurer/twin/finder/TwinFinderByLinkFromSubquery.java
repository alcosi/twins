package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinSearchId;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2727,
        name = "By link from subquery",
        description = "Runs a saved search and restricts the current search to twins linked with its result ids")
public class TwinFinderByLinkFromSubquery extends TwinFinderSubquery {

    @FeaturerParam(name = "Subquery search id",
            description = "Saved search whose twin ids restrict the link",
            order = 1)
    public static final FeaturerParamUUID subQueryTwinSearchId = new FeaturerParamUUIDTwinsTwinSearchId("subQueryTwinSearchId");

    @FeaturerParam(name = "Link id",
            description = "Link between the current twins and the subquery twins",
            order = 2)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Subquery twins are link src",
            description = "True: subquery twins are the link source and the current twins are the destination. False: the opposite",
            order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean sourceIsSrc = new FeaturerParamBoolean("sourceIsSrc");

    @FeaturerParam(name = "Exclude",
            description = "True: twins linked this way are excluded",
            order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @FeaturerParam(name = "Any of list",
            description = "True: a link to any subquery twin matches. False: a link to every subquery twin is required",
            order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean anyOfList = new FeaturerParamBoolean("anyOfList");

    @FeaturerParam(name = "Distinct",
            description = "Return a twin once when it is linked from several subquery twins",
            order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean distinct = new FeaturerParamBoolean("distinct");

    @FeaturerParam(name = "Subquery twins limit",
            description = "Max subquery twins to resolve; overflow is warned and truncated. Keep moderate: the ids are embedded as an IN list into the search",
            order = 7, optional = true, defaultValue = "1000")
    public static final FeaturerParamInt subqueryTwinsLimit = new FeaturerParamInt("subqueryTwinsLimit");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID link = linkId.extract(properties);
        boolean sourceSide = Boolean.TRUE.equals(sourceIsSrc.extract(properties));
        int limit = subqueryTwinsLimit.extract(properties);
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "subqueryTwinsLimit must be positive");
        Set<UUID> subqueryTwinIds = takeSubqueryTwinIds(subQueryTwinSearchId.extract(properties), namedParamsMap, limit);
        twinSearch.setDistinct(Boolean.TRUE.equals(distinct.extract(properties)));
        if (subqueryTwinIds.isEmpty()) {
            // an empty id set on a link means "any twin of this link", so pin the search to an id that matches nothing
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        boolean linkExclude = Boolean.TRUE.equals(exclude.extract(properties));
        boolean anyOf = Boolean.TRUE.equals(anyOfList.extract(properties));
        if (sourceSide)
            twinSearch.addLinkSrcTwinsId(link, subqueryTwinIds, linkExclude, anyOf);
        else
            twinSearch.addLinkDstTwinsId(link, subqueryTwinIds, linkExclude, anyOf);
    }
}
