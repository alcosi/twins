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
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dao.permission.PermissionGrantTwinRoleRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.domain.search.PermissionGrantTwinRoleSearch;
import org.twins.core.enums.twin.TwinRole;
import org.twins.core.service.auth.AuthService;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantTwinRoleSpecification.checkFieldLikeIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionGrantTwinRoleSearchService {

    private final AuthService authService;
    private final PermissionGrantTwinRoleRepository permissionGrantTwinRoleRepository;

    public PaginationResult<PermissionGrantTwinRoleEntity> findPermissionGrantTwinRoles(PermissionGrantTwinRoleSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantTwinRoleEntity> spec = createPermissionGrantTwinRoleSearchSpecification(search, domainId);
        Page<PermissionGrantTwinRoleEntity> ret = permissionGrantTwinRoleRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantTwinRoleEntity> createPermissionGrantTwinRoleSearchSpecification(PermissionGrantTwinRoleSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId,PermissionGrantTwinRoleEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, PermissionGrantTwinRoleEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionGrantTwinRoleEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, PermissionGrantTwinRoleEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, PermissionGrantTwinRoleEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionIdList(), false, false, PermissionGrantTwinRoleEntity.Fields.permissionId),
                checkUuidIn(search.getPermissionIdExcludeList(), true, false, PermissionGrantTwinRoleEntity.Fields.permissionId),
                checkUuidIn(search.getTwinClassIdList(), false, false, PermissionGrantTwinRoleEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, PermissionGrantTwinRoleEntity.Fields.twinClassId),
                checkFieldLikeIn(safeConvertToString(search.getTwinRoleList()), false, false, PermissionGrantTwinRoleEntity.Fields.twinRole),
                checkFieldLikeIn(safeConvertToString(search.getTwinRoleExcludeList()), true, false, PermissionGrantTwinRoleEntity.Fields.twinRole),
                checkUuidIn(search.getGrantedByUserIdList(), false, false, PermissionGrantTwinRoleEntity.Fields.grantedByUserId),
                checkUuidIn(search.getGrantedByUserIdExcludeList(), true, true, PermissionGrantTwinRoleEntity.Fields.grantedByUserId));
    }

    private Set<String> safeConvertToString(Set<TwinRole> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }

}
