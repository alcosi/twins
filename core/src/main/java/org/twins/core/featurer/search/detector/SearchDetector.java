package org.twins.core.featurer.search.detector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.SearchAliasEntity;
import org.twins.core.dao.search.SearchEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@FeaturerType(id = 28,
        name = "SearchBatcher",
        description = "Encapsulate logic to select searches from one alias into one batch")
@Slf4j
public abstract class SearchDetector extends Featurer {
    public List<SearchEntity> detect(SearchAliasEntity aliasEntity, List<SearchEntity> allAliasSearches) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, aliasEntity.getSearchDetectorParams(), new HashMap<>());
        return detect(properties, allAliasSearches);
    }

    public abstract List<SearchEntity> detect(Properties properties, List<SearchEntity> allAliasSearches) throws ServiceException;

}
