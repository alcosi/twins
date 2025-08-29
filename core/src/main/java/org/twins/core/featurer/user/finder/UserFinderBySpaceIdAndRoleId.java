package org.twins.core.featurer.user.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SpaceSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4301,
        name = "Find users by space id and role id",
        description = "")
public class UserFinderBySpaceIdAndRoleId extends UserFinder {
    @FeaturerParam(name = "Role id", description = "", order = 1)
    public static final FeaturerParamUUID roleId = new FeaturerParamUUID("roleId");

    @FeaturerParam(name = "Twin class id for space", description = "", order = 2)
    public static final FeaturerParamString twinClassId = new FeaturerParamString("twinClassId");

    @FeaturerParam(name = "Exclude space role search", description = "", order = 3)
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    protected void concatSearch(Properties properties, UserSearch userSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID extractedRoleId = roleId.extract(properties);
        UUID spaceId = UUID.fromString(namedParamsMap.get(twinClassId.extract(properties)));
        SpaceSearch spaceSearch = new SpaceSearch()
                .setRoleId(extractedRoleId)
                .setSpaceId(spaceId);
        userSearch.addSpace(spaceSearch, exclude.extract(properties));
    }
}
