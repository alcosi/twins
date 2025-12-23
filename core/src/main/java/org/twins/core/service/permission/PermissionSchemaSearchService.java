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
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.domain.search.PermissionSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionSchemaSearchService {
    private final AuthService authService;
    private final PermissionSchemaRepository permissionSchemaRepository;


    public PaginationResult<PermissionSchemaEntity> findPermissionSchemasByDomain(PermissionSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionSchemaEntity> spec = createPermissionSchemaSearchSpecification(search)
                .and(checkFieldUuid(domainId, PermissionSchemaEntity.Fields.domainId));
        Page<PermissionSchemaEntity> ret = permissionSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionSchemaEntity> createPermissionSchemaSearchSpecification(PermissionSchemaSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldLikeContainsIn(search.getNameLikeList(), false, false, PermissionSchemaEntity.Fields.name),
                checkFieldLikeContainsIn(search.getNameNotLikeList(), true, true, PermissionSchemaEntity.Fields.name),
                checkFieldLikeContainsIn(search.getDescriptionLikeList(), false, false, PermissionSchemaEntity.Fields.description),
                checkFieldLikeContainsIn(search.getDescriptionNotLikeList(), true, true, PermissionSchemaEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, true, PermissionSchemaEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionSchemaEntity.Fields.id),
                checkUuidIn(search.getBusinessAccountIdList(), false, true, PermissionSchemaEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, true, PermissionSchemaEntity.Fields.businessAccountId),
                checkUuidIn(search.getCreatedByUserIdList(), false, true, PermissionSchemaEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, PermissionSchemaEntity.Fields.createdByUserId));
    }
}
