package org.twins.core.featurer.user.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.SpaceSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4302,
        name = "Find users for head twin by space id and role id",
        description = "")
public class UserFinderHeadBySpaceIdAndRoleId extends UserFinderRequested {
    @FeaturerParam(name = "Param key", description = "", order = 1, optional = true, defaultValue = PARAM_TWIN_CLASS_ID)
    public static final FeaturerParamString paramKey = new FeaturerParamString("paramKey");

    @FeaturerParam(name = "Role id", description = "", order = 2)
    public static final FeaturerParamUUID roleId = new FeaturerParamUUID("roleId");

    @FeaturerParam(name = "Exclude space role search", description = "", order = 3)
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected void concatSearch(Properties properties, UserSearch userSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID extractedRoleId = roleId.extract(properties);
        UUID spaceId = getRequestedId(paramKey, properties, namedParamsMap);
        BasicSearch search = new BasicSearch();
        search.addTwinId(spaceId, false);
        List<TwinEntity> childTwins = twinSearchService.findTwins(search);
        if (CollectionUtils.isEmpty(childTwins)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_INCORRECT, "twin[" + spaceId + "] not found");
        }
        SpaceSearch spaceSearch = new SpaceSearch()
                .setRoleId(extractedRoleId)
                .setSpaceId(childTwins.getFirst().getHeadTwinId());
        userSearch.addSpace(spaceSearch, exclude.extract(properties));
    }
}
