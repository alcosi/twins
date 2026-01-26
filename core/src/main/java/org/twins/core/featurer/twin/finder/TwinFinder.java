package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.TwinSearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_27,
        name = "SearchCriteriaBuilder",
        description = "")
@Slf4j
public abstract class TwinFinder extends FeaturerTwins {
    public void concat(TwinSearch twinSearch, TwinSearchPredicateEntity twinSearchPredicateEntity, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinSearchPredicateEntity.getTwinFinderParams(), new HashMap<>());
        concat(twinSearch, properties, namedParamsMap);
    }

    public abstract void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException;

}
