package org.twins.core.featurer.search.function;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@FeaturerType(id = 25,
        name = "SearchCriteriaBuilder",
        description = "")
@Slf4j
public abstract class SearchCriteriaBuilder extends Featurer {
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, searchPredicateEntity.getSearchFunctionParams(), new HashMap<>());
        concat(twinSearch, searchPredicateEntity, properties, namedParamsMap);
    }

    public abstract void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException;

}
