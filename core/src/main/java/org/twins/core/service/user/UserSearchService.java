package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.BasicSearchList;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.enums.user.UserGroupType;
import org.twins.core.featurer.user.finder.UserFinder;
import org.twins.core.featurer.user.sorter.UserSorter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.user.UserSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserSearchService extends EntitySecureFindServiceImpl<UserSearchEntity> {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final TwinSearchService twinSearchService;
    private final UserSearchPredicateRepository userSearchPredicateRepository;
    private final FeaturerService featurerService;
    private final UserSearchRepository userSearchRepository;
    private final UserGroupRepository userGroupRepository;

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
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(UserSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public PaginationResult<UserEntity> findUsers(UUID searchId, UserSearch narrowSearch, Map<String, String> namedParamsMap, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.USER_SEARCH_UNLIMITED.equals(searchId)) {
            return findUsers(narrowSearch, pagination);
        }
        UserSearchEntity searchEntity = findEntitySafe(searchId);
        UserSearch mainSearch = new UserSearch();
        List<UserSearchPredicateEntity> searchPredicates = userSearchPredicateRepository.findByUserSearchId(searchEntity.getId());
        for (UserSearchPredicateEntity predicate : searchPredicates) {
            UserFinder userFinder = featurerService.getFeaturer(predicate.getUserFinderFeaturerId(), UserFinder.class);
            userFinder.concatSearch(predicate.getUserFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        return findUsers(mainSearch, pagination);
    }

    public PaginationResult<UserEntity> findUsers(UserSearch search, SimplePagination pagination) throws ServiceException {
        Specification<UserEntity> userSpec = createUserSpecification(search);

        if (search.getChildTwinSearches() != null && CollectionUtils.isNotEmpty(search.getChildTwinSearches().getSearches())) {
            Specification<UserEntity> twinSpec = search.getChildTwinSearches().getSearches().stream()
                    .filter(Objects::nonNull)
                    .map(this::createTwinSpecification)
                    .reduce((spec1, spec2) ->
                            search.getChildTwinSearches().isMatchAll()
                                    ? spec1.and(spec2)
                                    : spec1.or(spec2)
                    )
                    .orElse(null);

            if (twinSpec != null) {
                Specification<UserEntity> combinedSpec = userSpec.and(twinSpec);
                Page<UserEntity> page = userRepository.findAll(combinedSpec, PaginationUtils.pageableOffset(pagination));
                return PaginationUtils.convertInPaginationResult(page, pagination);
            }
        }
        userSpec = addSorting(search, pagination, userSpec);
        Page<UserEntity> page = userRepository.findAll(userSpec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    private Specification<UserEntity> createTwinSpecification(BasicSearch childSearch) {
        return (userRoot, query, cb) -> {
            if (query == null) {
                log.error("Null parameters in twin specification");
                return cb.disjunction();
            }

            try {
                Expression<UUID> userIdPath = userRoot.get(UserEntity.Fields.id);

                Subquery<Long> countSubquery = query.subquery(Long.class);
                Root<TwinEntity> twinRoot = countSubquery.from(TwinEntity.class);

                Predicate parentCondition = cb.equal(twinRoot.get(TwinEntity.Fields.headTwinId), userIdPath);
                Predicate searchConditions = twinSearchService
                        .createTwinEntitySearchSpecification(childSearch)
                        .toPredicate(twinRoot, query, cb);

                countSubquery.select(cb.count(twinRoot))
                        .where(cb.and(parentCondition, searchConditions));

                return cb.greaterThan(countSubquery, 0L);

            } catch (Exception e) {
                log.error("Child twins filter error", e);
                return cb.disjunction();
            }
        };
    }

    public Specification<UserEntity> createUserSpecification(UserSearch search) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        UUID businessAccountId = authService.getApiUser().getBusinessAccountId();
        return Specification.allOf(
                checkUserDomain(domainId),
                checkBusinessAccountId(businessAccountId),
                checkUuidIn(search.getUserIdList(), false, false, UserEntity.Fields.id),
                checkUuidIn(search.getUserIdExcludeList(), true, false, UserEntity.Fields.id),
                checkFieldLikeIn(search.getUserNameLikeList(), false, true, UserEntity.Fields.name),
                checkFieldLikeIn(search.getUserNameLikeExcludeList(), true, false, UserEntity.Fields.name),
                checkFieldLikeIn(search.getUserEmailLikeList(), false, true, UserEntity.Fields.email),
                checkFieldLikeIn(search.getUserEmailLikeExcludeList(), true, false, UserEntity.Fields.email),
                checkFieldNameOrEmailLikeIn(search.getUserNameOrEmailLikeList(), false, true),
                checkFieldNameOrEmailLikeIn(search.getUserNameOrEmailExcludeList(), true, false),
                checkUserGroupType(search.getUserGroupIdList(), false, true, businessAccountId, domainId),
                checkUserGroupType(search.getUserGroupIdExcludeList(), true, false, businessAccountId, domainId),
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true),
                checkSpaceRoleLikeIn(search.getSpaceList(), domainId, businessAccountId, false),
                checkSpaceRoleLikeIn(search.getSpaceExcludeList(), domainId, businessAccountId, true)
        );
    }

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

    private Specification<UserEntity> addSorting(UserSearch search, SimplePagination pagination, Specification<UserEntity> specification) throws ServiceException {
        UserSearchEntity searchEntity = search.getConfiguredSearch();
        if (searchEntity != null &&
                (searchEntity.isForceSorting() || pagination == null || pagination.getSort() == null)) {
            UserSorter userSorter = featurerService.getFeaturer(searchEntity.getUserSorterFeaturerId(), UserSorter.class);
            var sortFunction = userSorter.createSort(searchEntity.getUserSorterParams());
            if (sortFunction != null) {
                specification = sortFunction.apply(specification);
                if (pagination != null)
                    pagination.setSort(null);
            }
        }
        return specification;
    }

    public Specification<UserEntity> checkUserGroupType(final Collection<UUID> userGroupIds, final boolean exclude, final boolean or, UUID businessAccountId, UUID domainId) throws ServiceException {
        KitGrouped<UserGroupEntity, UUID, String> userGroupEntities = new KitGrouped<>(userGroupRepository.findByIdIn(userGroupIds), UserGroupEntity::getId, UserGroupEntity::getUserGroupTypeId);
        for (Map.Entry<String, List<UserGroupEntity>> entry : userGroupEntities.getGroupedMap().entrySet()) {
            if (entry.getKey().equals(UserGroupType.domainScopeDomainManage.name())) {
                return UserSpecification.checkUserGroupMapType1IdIn(userGroupIds, exclude, or);
            } else if (entry.getKey().equals(UserGroupType.domainScopeBusinessAccountManage.name())) {
                return UserSpecification.checkUserGroupMapType2IdIn(userGroupIds, businessAccountId , exclude, or);
            } else {
                return UserSpecification.checkUserGroupMapType3IdIn(userGroupIds, domainId, exclude, or);
            }
        }
        return null;
    }
}
