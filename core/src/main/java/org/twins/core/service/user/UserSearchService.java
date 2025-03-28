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
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.mappers.rest.twin.TwinSearchDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.UUID;

import static org.twins.core.dao.user.UserSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final TwinSearchService twinSearchService;
    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    public PaginationResult<UserEntity> findUsers(UserSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        UUID businessAccountId = authService.getApiUser().getBusinessAccountId();
        Specification<UserEntity> userSpec = createUserSpecification(search, domainId, businessAccountId);

        if (search.getChildTwins() != null) {
            Specification<UserEntity> combinedSpec = userSpec.and(createTwinSpecification(search.getChildTwins()));
            Page<UserEntity> page = userRepository.findAll(combinedSpec, PaginationUtils.pageableOffset(pagination));
            return PaginationUtils.convertInPaginationResult(page, pagination);
        }

        Page<UserEntity> page = userRepository.findAll(userSpec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    private Specification<UserEntity> createTwinSpecification(TwinSearchDTOv1 childSearch) {
        return (userRoot, query, cb) -> {
            if (query == null) {
                log.error("Null parameters in twin specification");
                return cb.disjunction();
            }

            try {
                Expression<UUID> userIdPath = userRoot.get(UserEntity.Fields.id);

                BasicSearch childTwinsSearch = twinSearchDTOReverseMapper.convert(childSearch);

                Subquery<Long> countSubquery = query.subquery(Long.class);
                Root<TwinEntity> twinRoot = countSubquery.from(TwinEntity.class);

                Predicate parentCondition = cb.equal(twinRoot.get(TwinEntity.Fields.headTwinId), userIdPath);
                Predicate searchConditions = twinSearchService
                        .createTwinEntitySearchSpecification(childTwinsSearch)
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
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true),
                checkSpaceRoleLikeIn(search.getSpaceList(), false),
                checkSpaceRoleLikeIn(search.getSpaceExcludeList(), true),
                checkSpaceRoleGroupLikeIn(search.getSpaceGroupList(), domainId, businessAccountId, false),
                checkSpaceRoleGroupLikeIn(search.getSpaceGroupExcludeList(), domainId, businessAccountId, true)
        );
    }
}
