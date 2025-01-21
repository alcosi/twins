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
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId, search.getPermissionSchemaIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.permissionId, search.getPermissionIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.permissionId, search.getPermissionIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId, search.getPropagationTwinClassIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId, search.getPropagationTwinClassIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId, search.getPropagationTwinStatusIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId, search.getPropagationTwinStatusIdExcludeList(), true, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.grantedByUserId, search.getGrantedByUserIdList(), false, false),
                checkUuidIn(PermissionGrantAssigneePropagationEntity.Fields.grantedByUserId, search.getGrantedByUserIdExcludeList(), true, true));
    }

}
