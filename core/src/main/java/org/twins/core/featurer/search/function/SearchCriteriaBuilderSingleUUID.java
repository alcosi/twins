package org.twins.core.featurer.search.function;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class SearchCriteriaBuilderSingleUUID extends SearchCriteriaBuilder {
    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID id = getId(properties, namedParamsMap);
        switch (searchPredicateEntity.getSearchField()) {
            case twinId:
                twinSearch.addTwinId(id, searchPredicateEntity.isExclude());
            case statusId:
                twinSearch.addStatusId(id, searchPredicateEntity.isExclude());
            case assigneeUserId:
                twinSearch.addAssigneeUserId(id, searchPredicateEntity.isExclude());
            case createdByUserId:
                twinSearch.addCreatedByUserId(id, searchPredicateEntity.isExclude());
            case twinClassId:
                twinSearch.addTwinClassId(id, searchPredicateEntity.isExclude());
            case headTwinId:
                twinSearch.addHeaderTwinId(id);  //todo add exclude
            case markerDataListOptionId:
                twinSearch.addMarkerDataListOptionId(id, searchPredicateEntity.isExclude());
            case tagDataListOptionId:
                twinSearch.addTagDataListOptionId(id, searchPredicateEntity.isExclude());
            case hierarchyTreeContainsId:
                twinSearch.addHierarchyTreeContainsId(id); //todo add exclude
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        }
    }

    protected abstract UUID getId(Properties properties, Map<String, String> namedParamsMap);
}
