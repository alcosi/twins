package org.twins.core.featurer.user.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_43,
        name = "UserFinder",
        description = "Find users")
@Slf4j
public abstract class UserFinder extends FeaturerTwins {
    public void concatSearch(HashMap<String, String> userFinderParams, UserSearch userSearch, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userFinderParams, new HashMap<>());
        log.info("Running featurer[{}].findUsers with params: {}", this.getClass().getSimpleName(), properties.toString());
        concatSearch(properties, userSearch, namedParamsMap);
    }

    protected abstract void concatSearch(Properties properties, UserSearch userSearch, Map<String, String> namedParamsMap) throws ServiceException;

}
