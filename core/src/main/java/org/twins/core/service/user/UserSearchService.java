package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.user.UserSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {
    private final AuthService authService;
    private final UserRepository userRepository;

    public PaginationResult<UserEntity> findUsers(UserSearch search, SimplePagination pagination) throws ServiceException {
        Specification<UserEntity> spec = createUserSpecification(search);
        Page<UserEntity> ret = userRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    public Specification<UserEntity> createUserSpecification(UserSearch search) throws ServiceException {
        return Specification.allOf(
                checkUserDomain(authService.getApiUser().getDomainId()),
                checkUuidIn(search.getUserIdList(), false, false, UserEntity.Fields.id),
                checkUuidIn(search.getUserIdExcludeList(), true, false, UserEntity.Fields.id),
                checkFieldLikeIn(search.getUserNameLikeList(), false, true, UserEntity.Fields.name),
                checkFieldLikeIn(search.getUserNameLikeExcludeList(), true, false, UserEntity.Fields.name),
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true),
                checkSpaceRoleLikeIn(search.getSpaceList(), false),
                checkSpaceRoleLikeIn(search.getSpaceExcludeList(), true),
                checkSpaceRoleGroupLikeIn(search.getSpaceList(), false),
                checkSpaceRoleGroupLikeIn(search.getSpaceExcludeList(), true)
        );
    }
}
