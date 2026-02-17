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
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationEntity;
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
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
public class UserGroupByAssigneePropagationSearchService {

    private final AuthService authService;
    private final UserGroupByAssigneePropagationRepository userGroupByAssigneePropagationRepository;

    @Transactional(readOnly = true)
    public UserGroupByAssigneePropagationEntity findPermissionAssigneePropagationById(UUID id) throws ServiceException {
        Optional<UserGroupByAssigneePropagationEntity> entity = userGroupByAssigneePropagationRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), UserGroupByAssigneePropagationEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                        checkFieldUuid(id, UserGroupByAssigneePropagationEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

    public PaginationResult<UserGroupByAssigneePropagationEntity> findPermissionAssigneePropagations(UserGroupByAssigneePropagationSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<UserGroupByAssigneePropagationEntity> spec = createPermissionAssigneePropagationSearchSpecification(search, domainId);
        Page<UserGroupByAssigneePropagationEntity> ret = userGroupByAssigneePropagationRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<UserGroupByAssigneePropagationEntity> createPermissionAssigneePropagationSearchSpecification(UserGroupByAssigneePropagationSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, UserGroupByAssigneePropagationEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, UserGroupByAssigneePropagationEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, UserGroupByAssigneePropagationEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getUserGroupIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.userGroupId),
                checkUuidIn(search.getUserGroupIdExcludeList(), true, false, UserGroupByAssigneePropagationEntity.Fields.userGroupId),
                checkUuidIn(search.getPropagationTwinClassIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinClassIdExcludeList(), true, false, UserGroupByAssigneePropagationEntity.Fields.propagationByTwinClassId),
                checkUuidIn(search.getPropagationTwinStatusIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getPropagationTwinStatusIdExcludeList(), true, false, UserGroupByAssigneePropagationEntity.Fields.propagationByTwinStatusId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, UserGroupByAssigneePropagationEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, UserGroupByAssigneePropagationEntity.Fields.createdByUserId));
    }

}
