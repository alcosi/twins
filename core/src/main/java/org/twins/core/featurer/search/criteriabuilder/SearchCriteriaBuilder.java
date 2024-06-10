package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_27,
        name = "SearchCriteriaBuilder",
        description = "")
@Slf4j
public abstract class SearchCriteriaBuilder extends FeaturerTwins {
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Map<String, String> namedParamsMap) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, searchPredicateEntity.getSearchCriteriaBuilderParams(), new HashMap<>());
        concat(twinSearch, searchPredicateEntity, properties, namedParamsMap);
    }

    public abstract void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException;

}
