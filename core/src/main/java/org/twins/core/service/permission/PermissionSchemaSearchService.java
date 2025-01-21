package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.domain.search.PermissionSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSchemaSearchService {
    private final AuthService authService;
    private final PermissionSchemaRepository permissionSchemaRepository;

    @Transactional(readOnly = true)
    public PermissionSchemaEntity findPermissionSchemaById(UUID id) throws ServiceException {
        Optional<PermissionSchemaEntity> entity = permissionSchemaRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), PermissionSchemaEntity.Fields.domainId),
                        checkFieldUuid(id, PermissionSchemaEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

    public PaginationResult<PermissionSchemaEntity> findPermissionSchemasByDomain(PermissionSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionSchemaEntity> spec = createPermissionSchemaSearchSpecification(search)
                .and(checkFieldUuid(domainId, PermissionSchemaEntity.Fields.domainId));
        Page<PermissionSchemaEntity> ret = permissionSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionSchemaEntity> createPermissionSchemaSearchSpecification(PermissionSchemaSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldLikeContainsIn(PermissionSchemaEntity.Fields.name, search.getNameLikeList(), false, false),
                checkFieldLikeContainsIn(PermissionSchemaEntity.Fields.name, search.getNameNotLikeList(), true, true),
                checkFieldLikeContainsIn(PermissionSchemaEntity.Fields.description, search.getDescriptionLikeList(), false, false),
                checkFieldLikeContainsIn(PermissionSchemaEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(PermissionSchemaEntity.Fields.id, search.getIdList(), false, true),
                checkUuidIn(PermissionSchemaEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(PermissionSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdList(), false, true),
                checkUuidIn(PermissionSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdExcludeList(), true, true),
                checkUuidIn(PermissionSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdList(), false, true),
                checkUuidIn(PermissionSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdExcludeList(), true, true));
    }
}
