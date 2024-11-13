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
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dto.rest.permission.PermissionSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionSchemaSpecification.checkDomainId;
import static org.twins.core.dao.specifications.permission.PermissionSchemaSpecification.checkFieldLikeIn;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSchemaSearchService {
    private final AuthService authService;
    private final PermissionSchemaRepository permissionSchemaRepository;

    public PaginationResult<PermissionSchemaEntity> findPermissionSchemasByDomain(PermissionSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionSchemaEntity> spec = createPermissionSchemaSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<PermissionSchemaEntity> ret = permissionSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionSchemaEntity> createPermissionSchemaSearchSpecification(PermissionSchemaSearch search) throws ServiceException {
        return Specification.where(
                        checkFieldLikeIn(PermissionSchemaEntity.Fields.name, search.getNameLikeList(), false, false))
                .and(checkFieldLikeIn(PermissionSchemaEntity.Fields.name, search.getNameNotLikeList(), true, true))
                .and(checkFieldLikeIn(PermissionSchemaEntity.Fields.description, search.getDescriptionLikeList(), false, false))
                .and(checkFieldLikeIn(PermissionSchemaEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.id, search.getIdList(), false, true))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.id, search.getIdExcludeList(), true, false))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdList(), false, true))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdExcludeList(), true, true))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdList(), false, true))
                .and(checkUuidIn(PermissionSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdExcludeList(), true, true))
                ;
    }
}
