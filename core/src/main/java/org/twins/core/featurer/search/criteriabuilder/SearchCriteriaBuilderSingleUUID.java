package org.twins.core.featurer.search.criteriabuilder;

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
                break;
            case statusId:
                twinSearch.addStatusId(id, searchPredicateEntity.isExclude());
                break;
            case linkId:
                twinSearch.addLinkDstTwinsId(id, null, searchPredicateEntity.isExclude());
                break;
            case assigneeUserId:
                twinSearch.addAssigneeUserId(id, searchPredicateEntity.isExclude());
                break;
            case createdByUserId:
                twinSearch.addCreatedByUserId(id, searchPredicateEntity.isExclude());
                break;
            case twinClassId:
                twinSearch.addTwinClassId(id, searchPredicateEntity.isExclude());
                break;
            case headTwinId:
                twinSearch.addHeaderTwinId(id);  //todo add exclude
                break;
            case markerDataListOptionId:
                twinSearch.addMarkerDataListOptionId(id, searchPredicateEntity.isExclude());
                break;
            case tagDataListOptionId:
                twinSearch.addTagDataListOptionId(id, searchPredicateEntity.isExclude());
                break;
            case hierarchyTreeContainsId:
                twinSearch.addHierarchyTreeContainsId(id); //todo add exclude
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        }
    }

    protected abstract UUID getId(Properties properties, Map<String, String> namedParamsMap);
}
