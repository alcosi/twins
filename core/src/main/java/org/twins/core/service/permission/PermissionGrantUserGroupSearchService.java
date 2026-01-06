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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupRepository;
import org.twins.core.domain.search.PermissionGrantUserGroupSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantUserGroupSpecification.checkDomainId;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
        return checkDomainId(domainId)
                        .and(checkUuidIn(search.getIdList(), false, false, PermissionGrantUserGroupEntity.Fields.id))
                        .and(checkUuidIn(search.getIdExcludeList(), true, false, PermissionGrantUserGroupEntity.Fields.id))
                        .and(checkUuidIn(search.getPermissionSchemaIdList(), false, false, PermissionGrantUserGroupEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, PermissionGrantUserGroupEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(search.getPermissionIdList(), false, false, PermissionGrantUserGroupEntity.Fields.permissionId))
                        .and(checkUuidIn(search.getPermissionIdExcludeList(), true, false, PermissionGrantUserGroupEntity.Fields.permissionId))
                        .and(checkUuidIn(search.getUserGroupIdList(), false, false, PermissionGrantUserGroupEntity.Fields.userGroupId))
                        .and(checkUuidIn(search.getUserGroupIdExcludeList(), true, false, PermissionGrantUserGroupEntity.Fields.userGroupId))
                        .and(checkUuidIn(search.getGrantedByUserIdList(), false, false, PermissionGrantUserGroupEntity.Fields.grantedByUserId))
                        .and(checkUuidIn(search.getGrantedByUserIdExcludeList(), true, true, PermissionGrantUserGroupEntity.Fields.grantedByUserId));
    }

}
