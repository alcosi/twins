package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.UserGroupSearch;
import org.twins.core.service.auth.AuthService;


import static org.twins.core.dao.specifications.usergroup.UserGroupSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupSearchService {
    private final AuthService authService;
    private final UserGroupRepository userGroupRepository;

    public PaginationResult<UserGroupEntity> findUserGroupsForDomain(UserGroupSearch search, SimplePagination pagination) throws ServiceException {
        Specification<UserGroupEntity> spec = createUserGroupSearchSpecification(search);
        Page<UserGroupEntity> ret = userGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<UserGroupEntity> createUserGroupSearchSpecification(UserGroupSearch search) throws ServiceException {
        return Specification.where(
                checkDomainId(authService.getApiUser().getDomainId())
                        .and(checkUuidIn(UserGroupEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(UserGroupEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.name, search.getNameLikeList(), false, true))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.name, search.getNameNotLikeList(), true, true))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.description, search.getDescriptionLikeList(), false, true))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.userGroupTypeId, search.getTypeList(), false, true))
                        .and(checkFieldLikeIn(UserGroupEntity.Fields.userGroupTypeId, search.getTypeExcludeList(), true, true))
        );
    }
}
