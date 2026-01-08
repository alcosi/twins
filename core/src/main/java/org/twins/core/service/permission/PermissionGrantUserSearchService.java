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
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dao.permission.PermissionGrantUserRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.domain.search.PermissionGrantUserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.permission.PermissionGrantUserSpecification.checkFieldUuid;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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

        return Specification.allOf(
                checkFieldUuid(domainId, PermissionGrantUserEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, true, PermissionGrantUserEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, PermissionGrantUserEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, true, PermissionGrantUserEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, true, PermissionGrantUserEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionIdList(), false, true, PermissionGrantUserEntity.Fields.permissionId),
                checkUuidIn(search.getPermissionIdExcludeList(), true, true, PermissionGrantUserEntity.Fields.permissionId),
                checkUuidIn(search.getUserIdList(), false, true, PermissionGrantUserEntity.Fields.userId),
                checkUuidIn(search.getUserIdExcludeList(), true, true, PermissionGrantUserEntity.Fields.userId),
                checkUuidIn(search.getGrantedByUserIdList(), false, true, PermissionGrantUserEntity.Fields.grantedByUserId),
                checkUuidIn(search.getGrantedByUserIdExcludeList(), true, true, PermissionGrantUserEntity.Fields.grantedByUserId));
    }
}
