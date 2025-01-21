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
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionGroupRepository;
import org.twins.core.domain.search.PermissionGroupSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.permission.PermissionGroupSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGroupSearchService {
    private final AuthService authService;
    private final PermissionGroupRepository permissionGroupRepository;

    public PaginationResult<PermissionGroupEntity> findPermissionGroupForDomain(PermissionGroupSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGroupEntity> spec = createPermissionGroupSearchSpecification(search)
                .and(checkDomainId(domainId, search.isShowSystemGroups()));
        Page<PermissionGroupEntity> ret = permissionGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGroupEntity> createPermissionGroupSearchSpecification(PermissionGroupSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.key, search.getKeyLikeList(), false, true),
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.key, search.getKeyNotLikeList(), true, true),
                checkUuidIn(PermissionGroupEntity.Fields.twinClassId, search.getTwinClassIdList(), false, false),
                checkUuidIn(PermissionGroupEntity.Fields.twinClassId, search.getTwinClassIdExcludeList(), true, true),
                checkUuidIn(PermissionGroupEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(PermissionGroupEntity.Fields.id, search.getIdExcludeList(), true, true),
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.name, search.getNameLikeList(), false, true),
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.name, search.getNameNotLikeList(), true, true),
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeContainsIn(PermissionGroupEntity.Fields.description, search.getDescriptionNotLikeList(), true, true));

    }
}
