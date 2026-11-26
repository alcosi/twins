package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.UserGroupField;
import org.twins.core.enums.sort.UserSortField;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.user.UserSpecification.*;

/**
 * Standard {@link EntitySearchService} heir: enum-driven sorting (name/email/status, default
 * name) and group counting by status. The only user-specific twist — the childTwinSearches
 * subquery weaving — lives inside {@link #createFilterSpecification}, where the contract expects
 * all filtering, so the search/count/configured pipelines run unchanged.
 */
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserSearchService extends EntitySearchService<UserSearch, UserEntity, UserSortField, UserGroupField> {
    private final UserRepository userRepository;
    private final TwinSearchService twinSearchService;

    @Override
    public JpaSpecificationExecutor<UserEntity> jpaSpecificationExecutor() {
        return userRepository;
    }

    @Override
    public UserSearch newSearch() {
        return new UserSearch();
    }

    @Override
    protected Class<UserEntity> entityClass() {
        return UserEntity.class;
    }

    /**
     * User predicates plus the childTwinSearches subquery weaving. businessAccountId is pulled
     * from the API user (the contract has no slot for it); domainId arrives as a parameter.
     */
    @Override
    public Specification<UserEntity> createFilterSpecification(UserSearch search, UUID domainId, Locale locale) throws ServiceException {
        UUID businessAccountId = authService.getApiUser().getBusinessAccountId();
        Specification<UserEntity> spec = Specification.allOf(
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
                checkUserGroupIdIn(search.getUserGroupIdList(), false, true, businessAccountId, domainId),
                checkUserGroupIdIn(search.getUserGroupIdExcludeList(), true, false, businessAccountId, domainId),
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true),
                checkSpaceRoleLikeIn(search.getSpaceList(), domainId, businessAccountId, false),
                checkSpaceRoleLikeIn(search.getSpaceExcludeList(), domainId, businessAccountId, true)
        );
        Specification<UserEntity> twinSpec = childTwinSearchesSpec(search);
        return twinSpec == null ? spec : spec.and(twinSpec);
    }

    @Override
    public Specification<UserEntity> createSortSpecification(UserSortField sortField, SortDirection sortDirection, Locale locale) {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, UserEntity.Fields.name);
            case email -> toSortSpecification(ascending, UserEntity.Fields.email);
            case status -> toSortSpecification(ascending, UserEntity.Fields.userStatusId);
        };
    }

    @Override
    protected UserSortField defaultSortField() {
        return UserSortField.name;
    }

    @Override
    public String convertToEntityField(UserGroupField groupField) {
        return switch (groupField) {
            case status -> UserEntity.Fields.userStatusId;
        };
    }

    @Override
    public void mapGroupedField(UserEntity entity, UserGroupField field, Object o) {
        switch (field) {
            // user_status_id is @Enumerated(STRING): grouped count rows carry the raw String
            case status -> entity.setUserStatusId(o == null ? null : UserStatus.valueOf(o.toString()));
        }
    }

    @Override
    protected UserEntity newEntity() {
        return new UserEntity();
    }

    /**
     * One subquery per child twin search, combined by AND when matchAll and by OR otherwise;
     * null when the search carries no child twin searches.
     */
    private Specification<UserEntity> childTwinSearchesSpec(UserSearch search) {
        if (search.getChildTwinSearches() == null || CollectionUtils.isEmpty(search.getChildTwinSearches().getSearches()))
            return null;
        return search.getChildTwinSearches().getSearches().stream()
                .filter(Objects::nonNull)
                .map(this::createTwinSpecification)
                .reduce((spec1, spec2) ->
                        search.getChildTwinSearches().isMatchAll()
                                ? spec1.and(spec2)
                                : spec1.or(spec2)
                )
                .orElse(null);
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
}
