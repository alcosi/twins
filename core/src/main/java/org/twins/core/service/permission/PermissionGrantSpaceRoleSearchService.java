package org.twins.core.service.permission;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
                checkUuidIn(search.getIdList(), false, false, PermissionGrantSpaceRoleEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionGrantSpaceRoleEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, PermissionGrantSpaceRoleEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, PermissionGrantSpaceRoleEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionIdList(), false, false, PermissionGrantSpaceRoleEntity.Fields.permissionId),
                checkUuidIn(search.getPermissionIdExcludeList(), true, false, PermissionGrantSpaceRoleEntity.Fields.permissionId),
                checkUuidIn(search.getSpaceRoleIdList(), false, false, PermissionGrantSpaceRoleEntity.Fields.spaceRoleId),
                checkUuidIn(search.getSpaceRoleIdExcludeList(), true, false, PermissionGrantSpaceRoleEntity.Fields.spaceRoleId),
                checkUuidIn(search.getGrantedByUserIdList(), false, false, PermissionGrantSpaceRoleEntity.Fields.grantedByUserId),
                checkUuidIn(search.getGrantedByUserIdExcludeList(), true, true, PermissionGrantSpaceRoleEntity.Fields.grantedByUserId));
    }

}
