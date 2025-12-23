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
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionGroupRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.PermissionGroupSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.permission.PermissionGroupSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionGroupSearchService {
    private final AuthService authService;
    private final PermissionGroupRepository permissionGroupRepository;

    @Transactional(readOnly = true)
    public PermissionGroupEntity findPermissionGroupByKey(String id) throws ServiceException {
        Optional<PermissionGroupEntity> entity = permissionGroupRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), PermissionGroupEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                        checkFieldStringLike(id, PermissionGroupEntity.Fields.key)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

    public PaginationResult<PermissionGroupEntity> findPermissionGroupForDomain(PermissionGroupSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionGroupEntity> spec = createPermissionGroupSearchSpecification(search)
                .and(checkDomainId(domainId, search.isShowSystemGroups()));
        Page<PermissionGroupEntity> ret = permissionGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionGroupEntity> createPermissionGroupSearchSpecification(PermissionGroupSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldLikeContainsIn(search.getKeyLikeList(), false, true, PermissionGroupEntity.Fields.key),
                checkFieldLikeContainsIn(search.getKeyNotLikeList(), true, true, PermissionGroupEntity.Fields.key),
                checkUuidIn(search.getTwinClassIdList(), false, false, PermissionGroupEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, true, PermissionGroupEntity.Fields.twinClassId),
                checkUuidIn(search.getIdList(), false, false, PermissionGroupEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, PermissionGroupEntity.Fields.id),
                checkFieldLikeContainsIn(search.getNameLikeList(), false, true, PermissionGroupEntity.Fields.name),
                checkFieldLikeContainsIn(search.getNameNotLikeList(), true, true, PermissionGroupEntity.Fields.name),
                checkFieldLikeContainsIn(search.getDescriptionLikeList(), false, true, PermissionGroupEntity.Fields.description),
                checkFieldLikeContainsIn(search.getDescriptionNotLikeList(), true, true, PermissionGroupEntity.Fields.description));

    }
}
