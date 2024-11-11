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
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaUserGroupRepository;
import org.twins.core.domain.permission.PermissionSchemaUserGroupSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionSchemaSpecification.checkDomainId;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSchemaUserGroupSearchService {

    private final AuthService authService;
    private final PermissionSchemaUserGroupRepository permissionSchemaUserGroupRepository;

    public PaginationResult<PermissionSchemaUserGroupEntity> findPermissionSchemaUserGroups(PermissionSchemaUserGroupSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionSchemaUserGroupEntity> spec = createPermissionSchemaUserGroupSearchSpecification(search, domainId);
        Page<PermissionSchemaUserGroupEntity> ret = permissionSchemaUserGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionSchemaUserGroupEntity> createPermissionSchemaUserGroupSearchSpecification(PermissionSchemaUserGroupSearch search, UUID domainId) {
        return Specification.where(
                checkDomainId(domainId)
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.permissionId, search.getPermissionIdList(), false, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.userGroupId, search.getUserGroupIdList(), false, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.userGroupId, search.getUserGroupIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, false))
                        .and(checkUuidIn(PermissionSchemaUserGroupEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true))
        );
    }

}
