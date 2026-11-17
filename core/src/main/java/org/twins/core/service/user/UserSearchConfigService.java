package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.*;
import org.twins.core.domain.search.BasicSearchList;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.featurer.user.finder.UserFinder;
import org.twins.core.featurer.user.sorter.UserSorter;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchConfigService extends ConfiguredSearchService<UserSearchEntity, UserSearchPredicateEntity, UserSearch, UserEntity> {
    private final UserSearchRepository userSearchRepository;
    private final UserSearchPredicateRepository userSearchPredicateRepository;
    private final UserSearchService userSearchService;
    private final UserRepository userRepository;

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
    protected UserSearchService entitySearchService() {
        return userSearchService;
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
    protected Class<? extends SearchSorter<UserEntity>> sorterClass() {
        return UserSorter.class;
    }

    @Override
    protected Specification<UserEntity> createFilterSpecification(UserSearch search, UUID domainId, Locale locale) throws ServiceException {
        return userSearchService.createUserSpecification(search);
    }

    @Override
    protected JpaSpecificationExecutor<UserEntity> searchExecutor() {
        return userRepository;
    }

    @Override
    protected PaginationResult<UserEntity> runFreeSearch(UserSearch search, SimplePagination pagination) throws ServiceException {
        return userSearchService.findUsers(search, pagination);
    }

    /** childTwinSearches narrowing cannot be expressed by the generic @SearchFilter scan. */
    @Override
    protected void narrowSearchSpecific(UserSearch mainSearch, UserSearch narrowSearch) {
        if (narrowSearch.getChildTwinSearches() == null) {
            return;
        }
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

    /**
     * Users keep a custom configured path: findUsers additionally weaves childTwinSearches
     * subquery specs (and historically skips the sorter when they are present).
     */
    @Override
    protected PaginationResult<UserEntity> runConfiguredSearch(UserSearch mainSearch, SimplePagination pagination) throws ServiceException {
        return userSearchService.findUsers(mainSearch, pagination);
    }
}
