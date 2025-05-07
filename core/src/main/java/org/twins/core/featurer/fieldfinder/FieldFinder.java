package org.twins.core.featurer.fieldfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.TYPE_32,
        name = "FieldFinder",
        description = "Find class fields")
@Slf4j
public abstract class FieldFinder extends FeaturerTwins {
    public TwinClassFieldSearch createSearch(HashMap<String, String> fieldFinderParams, UUID twinClassId) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldFinderParams, new HashMap<>());
        log.info("Running featurer[{}].findFields with params: {}", this.getClass().getSimpleName(), properties.toString());
        TwinClassFieldSearch twinClassFieldSearch = new TwinClassFieldSearch();
        twinClassFieldSearch.addTwinClassId(twinClassId, true, false);
        createSearch(properties, twinClassId, twinClassFieldSearch);
        return twinClassFieldSearch;
    }

    protected abstract void createSearch(Properties properties, UUID twinClassId, TwinClassFieldSearch fieldSearch) throws ServiceException;
}
