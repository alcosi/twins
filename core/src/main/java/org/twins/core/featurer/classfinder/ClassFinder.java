package org.twins.core.featurer.classfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_39,
        name = "ClassFinder",
        description = "Find classes")
@Slf4j
public abstract class ClassFinder  extends FeaturerTwins {

    public TwinClassSearch createSearch(HashMap<String, String> classFinderParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, classFinderParams);
        log.info("Running featurer[{}].findClasses with params: {}", this.getClass().getSimpleName(), properties.toString());
        TwinClassSearch twinClassSearch = new TwinClassSearch();
        concatSearch(properties, twinClassSearch);
        return twinClassSearch;
    }

    public void concatSearch(HashMap<String, String> classFinderParams, TwinClassSearch classSearch) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, classFinderParams);
        log.info("Running featurer[{}].findClasses with params: {}", this.getClass().getSimpleName(), properties.toString());
        concatSearch(properties, classSearch);
    }

    protected abstract void concatSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException;

}
