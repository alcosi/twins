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
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.domain.search.PermissionGrantSpaceRoleSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGrantSpaceRoleSearchService {

    private final AuthService authService;
    private final PermissionGrantSpaceRoleRepository permissionGrantSpaceRoleRepository;

    public PaginationResult<PermissionGrantSpaceRoleEntity> findPermissionGrantSpaceRoles(PermissionGrantSpaceRoleSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantSpaceRoleEntity> spec = createPermissionGrantSpaceRoleSearchSpecification(search, domainId);
        Page<PermissionGrantSpaceRoleEntity> ret = permissionGrantSpaceRoleRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantSpaceRoleEntity> createPermissionGrantSpaceRoleSearchSpecification(PermissionGrantSpaceRoleSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, PermissionGrantSpaceRoleEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.permissionId, search.getPermissionIdList(), false, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.spaceRoleId, search.getSpaceRoleIdList(), false, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.spaceRoleId, search.getSpaceRoleIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, false),
                checkUuidIn(PermissionGrantSpaceRoleEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true));
    }

}
