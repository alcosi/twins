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
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dao.permission.PermissionGrantUserRepository;
import org.twins.core.domain.search.PermissionGrantUserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantUserSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGrantUserSearchService {
    private final AuthService authService;
    private final PermissionGrantUserRepository permissionGrantUserRepository;

    public PaginationResult<PermissionGrantUserEntity> findPermissionGrantUsersByDomain(PermissionGrantUserSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantUserEntity> spec = createPermissionGrantUserSearchSpecification(search, domainId);
        Page<PermissionGrantUserEntity> ret = permissionGrantUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantUserEntity> createPermissionGrantUserSearchSpecification(PermissionGrantUserSearch search, UUID domainId) throws ServiceException {
        return Specification.where(
                checkDomainId(domainId))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.id, search.getIdList(), false, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.id, search.getIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.permissionId, search.getPermissionIdList(), false, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.userId, search.getUserIdList(), false, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.userId, search.getUserIdExcludeList(), true, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, true))
                        .and(checkUuidIn(PermissionGrantUserEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true));
    }
}
