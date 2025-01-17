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
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.domain.search.PermissionGrantAssigneePropagationSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkDomainId;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationSearchService {

    private final AuthService authService;
    private final PermissionGrantAssigneePropagationRepository permissionGrantAssigneePropagationRepository;

    public PaginationResult<PermissionGrantAssigneePropagationEntity> findPermissionAssigneePropagations(PermissionGrantAssigneePropagationSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGrantAssigneePropagationEntity> spec = createPermissionAssigneePropagationSearchSpecification(search, domainId);
        Page<PermissionGrantAssigneePropagationEntity> ret = permissionGrantAssigneePropagationRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGrantAssigneePropagationEntity> createPermissionAssigneePropagationSearchSpecification(PermissionGrantAssigneePropagationSearch search, UUID domainId) {
        return Specification.allOf(
                checkDomainId(domainId, PermissionGrantAssigneePropagationEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
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
