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
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.domain.search.PermissionSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.permission.PermissionSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSearchService {
    private final AuthService authService;
    private final PermissionRepository permissionRepository;

    public PaginationResult<PermissionEntity> findPermissionForDomain(PermissionSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionEntity> spec = createPermissionSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<PermissionEntity> ret = permissionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionEntity> createPermissionSearchSpecification(PermissionSearch search) {
        return Specification.where(
                checkFieldLikeIn(PermissionEntity.Fields.key, search.getKeyLikeList(), false, true)
                        .and(checkFieldLikeIn(PermissionEntity.Fields.key, search.getKeyNotLikeList(), true, true))
                        .and(checkUuidIn(PermissionEntity.Fields.id, search.getIdList(), false, true))
                        .and(checkUuidIn(PermissionEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkFieldLikeIn(PermissionEntity.Fields.name, search.getNameLikeList(), false, true))
                        .and(checkFieldLikeIn(PermissionEntity.Fields.name, search.getNameNotLikeList(), true, true))
                        .and(checkFieldLikeIn(PermissionEntity.Fields.description, search.getDescriptionLikeList(), false, true))
                        .and(checkFieldLikeIn(PermissionEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(PermissionEntity.Fields.permissionGroupId, search.getGroupIdList(), false, true))
                        .and(checkUuidIn(PermissionEntity.Fields.permissionGroupId, search.getGroupIdExcludeList(), true, true))
        );
    }
}