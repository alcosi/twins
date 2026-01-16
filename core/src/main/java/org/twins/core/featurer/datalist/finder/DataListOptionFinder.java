package org.twins.core.featurer.datalist.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_51,
        name = "DataListOptionFinder",
        description = "Find data list options")
@Slf4j
public abstract class DataListOptionFinder extends FeaturerTwins {

    public void concatSearch(HashMap<String, String> optionFinderParams, DataListOptionSearch optionSearch, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, optionFinderParams, new HashMap<>());
        log.info("Running featurer[{}].findFields with params: {}", this.getClass().getSimpleName(), properties.toString());
        concatSearch(properties, optionSearch, namedParamsMap);
    }

    public abstract void concatSearch(Properties properties, DataListOptionSearch optionSearch, Map<String, String> namedParamsMap) throws ServiceException;
}
