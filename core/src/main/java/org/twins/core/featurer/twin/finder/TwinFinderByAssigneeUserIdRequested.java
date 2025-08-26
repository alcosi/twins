package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2703,
        name = "By assignee user id (requested)",
        description = "")
public abstract class TwinFinderByAssigneeUserIdRequested extends TwinFinderRequested {
    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        Set<UUID> userIdSet = getRequestedIds(properties, namedParamsMap);
        if (CollectionUtils.isEmpty(userIdSet)) {
            return;
        }
        twinSearch.addAssigneeUserId(userIdSet, exclude.extract(properties));
    }
}
