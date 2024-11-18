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
import org.twins.core.dao.permission.PermissionSchemaUserEntity;
import org.twins.core.dao.permission.PermissionSchemaUserRepository;
import org.twins.core.domain.search.PermissionSchemaUserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionSchemaUserSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSchemaUserSearchService {
    private final AuthService authService;
    private final PermissionSchemaUserRepository permissionSchemaUserRepository;

    public PaginationResult<PermissionSchemaUserEntity> findPermissionSchemaUsersByDomain(PermissionSchemaUserSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionSchemaUserEntity> spec = createPermissionSchemaUserSearchSpecification(search, domainId);
        Page<PermissionSchemaUserEntity> ret = permissionSchemaUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionSchemaUserEntity> createPermissionSchemaUserSearchSpecification(PermissionSchemaUserSearch search, UUID domainId) throws ServiceException {
        return Specification.where(
                checkDomainId(domainId))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.id, search.getIdList(), false, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.id, search.getIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.permissionId, search.getPermissionIdList(), false, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.userId, search.getUserIdList(), false, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.userId, search.getUserIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, true))
                        .and(checkUuidIn(PermissionSchemaUserEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true));
    }
}
