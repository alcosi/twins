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
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dao.permission.PermissionGrantTwinRoleRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.domain.TwinRole;
import org.twins.core.domain.search.PermissionGrantTwinRoleSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.twins.core.dao.specifications.CommonSpecification.checkDomainId;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantTwinRoleSpecification.checkFieldLikeIn;


@Slf4j
@Service
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
                checkDomainId(domainId,PermissionGrantTwinRoleEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.permissionId, search.getPermissionIdList(), false, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.twinClassId, search.getTwinClassIdList(), false, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.twinClassId, search.getTwinClassIdExcludeList(), true, false),
                checkFieldLikeIn(PermissionGrantTwinRoleEntity.Fields.twinRole, safeConvertToString(search.getTwinRoleList()), false, false),
                checkFieldLikeIn(PermissionGrantTwinRoleEntity.Fields.twinRole, safeConvertToString(search.getTwinRoleExcludeList()), true, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, false),
                checkUuidIn(PermissionGrantTwinRoleEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true));
    }

    private Set<String> safeConvertToString(Set<TwinRole> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }

}
