package org.twins.core.featurer.search.criteriabuilder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchPredicateEntity;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2702,
        name = "CurrentUser",
        description = "")
public class SearchCriteriaBuilderCurrentUser extends SearchCriteriaBuilder {
    @Autowired
    @Lazy
    AuthService authService;

    @Override
    public void concat(TwinSearch twinSearch, SearchPredicateEntity searchPredicateEntity, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID id = authService.getApiUser().getUserId();
        switch (searchPredicateEntity.getSearchField()) {
            case twinId:
                twinSearch.addTwinId(id, searchPredicateEntity.isExclude());
            case assigneeUserId:
                twinSearch.addAssigneeUserId(id, searchPredicateEntity.isExclude());
            case createdByUserId:
                twinSearch.addCreatedByUserId(id, searchPredicateEntity.isExclude());
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Incorrect criteria builder[" + this.getClass().getSimpleName() + "] for field[" + searchPredicateEntity.getSearchField() + "]");
        }
    }
}
