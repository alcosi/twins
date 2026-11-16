package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.*;
import org.twins.core.domain.search.BasicSearchList;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.user.finder.UserFinder;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchConfigService extends ConfiguredSearchService<UserSearchEntity, UserSearchPredicateEntity, UserSearch, UserEntity> {
    private final UserSearchRepository userSearchRepository;
    private final UserSearchPredicateRepository userSearchPredicateRepository;
    private final UserSearchService userSearchService;

    @Override
    public CrudRepository<UserSearchEntity, UUID> entityRepository() {
        return userSearchRepository;
    }

    @Override
    public Function<UserSearchEntity, UUID> entityGetIdFunction() {
        return UserSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logShort(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(UserSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.UserSearch.UNLIMITED;
    }

    @Override
    protected UserSearch emptySearch() {
        return new UserSearch();
    }

    @Override
    protected List<UserSearchPredicateEntity> loadPredicates(UserSearchEntity searchEntity) {
        return userSearchPredicateRepository.findByUserSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<UserSearch>> finderClass() {
        return UserFinder.class;
    }

    @Override
    protected PaginationResult<UserEntity> runFreeSearch(UserSearch search, SimplePagination pagination) throws ServiceException {
        return userSearchService.findUsers(search, pagination);
    }

    @Override
    protected PaginationResult<UserEntity> runConfiguredSearch(UserSearch mainSearch, SimplePagination pagination) throws ServiceException {
        return userSearchService.findUsers(mainSearch, pagination);
    }

    @Override
    protected void narrowSearch(UserSearch mainSearch, UserSearch narrowSearch) {
        if (narrowSearch == null)
            return;

        for (Pair<Function<UserSearch, Set>, BiConsumer<UserSearch, Set>> functionPair : UserSearch.FUNCTIONS) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }

        if (narrowSearch.getChildTwinSearches() != null) {
            if (mainSearch.getChildTwinSearches() == null) {
                mainSearch.setChildTwinSearches(narrowSearch.getChildTwinSearches());
            } else {
                BasicSearchList mainChild = mainSearch.getChildTwinSearches();
                BasicSearchList narrowChild = narrowSearch.getChildTwinSearches();

                if (mainChild.getSearches() != null && narrowChild.getSearches() != null) {
                    mainChild.getSearches().retainAll(narrowChild.getSearches());
                }

                mainChild.setMatchAll(mainChild.isMatchAll() || narrowChild.isMatchAll());
            }
        }

    }
}
