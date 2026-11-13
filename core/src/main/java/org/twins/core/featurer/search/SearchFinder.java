package org.twins.core.featurer.search;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for preset-search finder featurers. Each implementation accumulates
 * criteria into the given domain search object. Runtime context is passed via
 * {@code namedParamsMap} (e.g. current twin class id, current twin id).
 */
@Slf4j
public abstract class SearchFinder<S> extends FeaturerTwins {
    public void concatSearch(HashMap<String, String> finderParams, S search, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, finderParams);
        log.info("Running featurer[{}].concatSearch with params: {}", this.getClass().getSimpleName(), properties.toString());
        concatSearch(properties, search, namedParamsMap);
    }

    protected abstract void concatSearch(Properties properties, S search, Map<String, String> namedParamsMap) throws ServiceException;
}
