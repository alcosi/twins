package org.twins.core.service.user;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.twins.core.dao.user.UserSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final TwinSearchService twinSearchService;

    public PaginationResult<UserEntity> findUsers(UserSearch search, Map<String, String> params, UUID searchId, SimplePagination pagination) throws ServiceException {
        //todo this mock object
        return findUsers(search, pagination);
    }

    public PaginationResult<UserEntity> findUsers(UserSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        UUID businessAccountId = authService.getApiUser().getBusinessAccountId();
        Specification<UserEntity> userSpec = createUserSpecification(search, domainId, businessAccountId);

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

    public Specification<UserEntity> createUserSpecification(UserSearch search, UUID domainId, UUID businessAccountId) throws ServiceException {
        return Specification.allOf(
                checkUserDomain(domainId),
                checkUuidIn(search.getUserIdList(), false, false, UserEntity.Fields.id),
                checkUuidIn(search.getUserIdExcludeList(), true, false, UserEntity.Fields.id),
                checkFieldLikeIn(search.getUserNameLikeList(), false, true, UserEntity.Fields.name),
                checkFieldLikeIn(search.getUserNameLikeExcludeList(), true, false, UserEntity.Fields.name),
                checkFieldLikeIn(search.getUserEmailLikeList(), false, true, UserEntity.Fields.email),
                checkFieldLikeIn(search.getUserEmailLikeExcludeList(), true, false, UserEntity.Fields.email),
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true),
                checkSpaceRoleLikeIn(search.getSpaceList(), domainId, businessAccountId, false),
                checkSpaceRoleLikeIn(search.getSpaceExcludeList(), domainId, businessAccountId, true)
        );
    }
}
