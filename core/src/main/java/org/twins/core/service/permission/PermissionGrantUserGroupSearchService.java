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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupRepository;
import org.twins.core.domain.search.PermissionGrantUserGroupSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantUserGroupSpecification.checkDomainId;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGrantUserGroupSearchService {

    private final AuthService authService;
    private final PermissionGrantUserGroupRepository permissionGrantUserGroupRepository;

    public PaginationResult<PermissionGrantUserGroupEntity> findPermissionGrantUserGroups(PermissionGrantUserGroupSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantUserGroupEntity> spec = createPermissionGrantUserGroupSearchSpecification(search, domainId);
        Page<PermissionGrantUserGroupEntity> ret = permissionGrantUserGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantUserGroupEntity> createPermissionGrantUserGroupSearchSpecification(PermissionGrantUserGroupSearch search, UUID domainId) {
        return Specification.where(
                checkDomainId(domainId)
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.permissionId, search.getPermissionIdList(), false, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.userGroupId, search.getUserGroupIdList(), false, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.userGroupId, search.getUserGroupIdExcludeList(), true, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, false))
                        .and(checkUuidIn(PermissionGrantUserGroupEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true))
        );
    }

}
