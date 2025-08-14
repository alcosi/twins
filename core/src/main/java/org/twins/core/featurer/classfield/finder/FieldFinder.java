package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_32,
        name = "FieldFinder",
        description = "Find class fields")
@Slf4j
public abstract class FieldFinder extends FeaturerTwins {
    public static final String PARAM_CURRENT_TWIN_CLASS_ID = "currentTwinClassId";
    public static final String PARAM_CURRENT_TWIN_ID = "currentTwinId";

    public void concatSearch(HashMap<String, String> fieldFinderParams, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldFinderParams, new HashMap<>());
        log.info("Running featurer[{}].findFields with params: {}", this.getClass().getSimpleName(), properties.toString());
        concatSearch(properties, fieldSearch, namedParamsMap);
    }

    protected abstract void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException;
}
