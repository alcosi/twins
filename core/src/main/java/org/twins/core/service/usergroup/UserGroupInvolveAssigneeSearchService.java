package org.twins.core.service.usergroup;

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
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeRepository;
import org.twins.core.domain.search.UserGroupByAssigneePropagationSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeSearchService {

    private final AuthService authService;
    private final UserGroupInvolveAssigneeRepository userGroupInvolveAssigneeRepository;

    @Transactional(readOnly = true)
    public UserGroupInvolveAssigneeEntity findPermissionAssigneePropagationById(UUID id) throws ServiceException {
        Optional<UserGroupInvolveAssigneeEntity> entity = userGroupInvolveAssigneeRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), UserGroupInvolveAssigneeEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                        checkFieldUuid(id, UserGroupInvolveAssigneeEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

    public PaginationResult<UserGroupInvolveAssigneeEntity> findPermissionAssigneePropagations(UserGroupByAssigneePropagationSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<UserGroupInvolveAssigneeEntity> spec = createPermissionAssigneePropagationSearchSpecification(search, domainId);
        Page<UserGroupInvolveAssigneeEntity> ret = userGroupInvolveAssigneeRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<UserGroupInvolveAssigneeEntity> createPermissionAssigneePropagationSearchSpecification(UserGroupByAssigneePropagationSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, UserGroupInvolveAssigneeEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, UserGroupInvolveAssigneeEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, UserGroupInvolveAssigneeEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getUserGroupIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.userGroupId),
                checkUuidIn(search.getUserGroupIdExcludeList(), true, false, UserGroupInvolveAssigneeEntity.Fields.userGroupId),
                checkUuidIn(search.getPropagationTwinClassIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinClassIdExcludeList(), true, false, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinStatusIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getPropagationTwinStatusIdExcludeList(), true, false, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, UserGroupInvolveAssigneeEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, UserGroupInvolveAssigneeEntity.Fields.createdByUserId));
    }

}
