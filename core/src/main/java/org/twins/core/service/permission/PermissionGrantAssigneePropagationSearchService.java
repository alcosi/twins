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
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.PermissionGrantAssigneePropagationSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationSearchService {

    private final AuthService authService;
    private final PermissionGrantAssigneePropagationRepository permissionGrantAssigneePropagationRepository;

    @Transactional(readOnly = true)
    public PermissionGrantAssigneePropagationEntity findPermissionAssigneePropagationById(UUID id) throws ServiceException {
        Optional<PermissionGrantAssigneePropagationEntity> entity = permissionGrantAssigneePropagationRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), PermissionGrantAssigneePropagationEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                        checkFieldUuid(id, PermissionGrantAssigneePropagationEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

    public PaginationResult<PermissionGrantAssigneePropagationEntity> findPermissionAssigneePropagations(PermissionGrantAssigneePropagationSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantAssigneePropagationEntity> spec = createPermissionAssigneePropagationSearchSpecification(search, domainId);
        Page<PermissionGrantAssigneePropagationEntity> ret = permissionGrantAssigneePropagationRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantAssigneePropagationEntity> createPermissionAssigneePropagationSearchSpecification(PermissionGrantAssigneePropagationSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, PermissionGrantAssigneePropagationEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionGrantAssigneePropagationEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.permissionId),
                checkUuidIn(search.getPermissionIdExcludeList(), true, false, PermissionGrantAssigneePropagationEntity.Fields.permissionId),
                checkUuidIn(search.getPropagationTwinClassIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinClassIdExcludeList(), true, false, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinStatusIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getPropagationTwinStatusIdExcludeList(), true, false, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getGrantedByUserIdList(), false, false, PermissionGrantAssigneePropagationEntity.Fields.grantedByUserId),
                checkUuidIn(search.getGrantedByUserIdExcludeList(), true, true, PermissionGrantAssigneePropagationEntity.Fields.grantedByUserId));
    }

}
