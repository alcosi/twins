package org.twins.core.service.twinflow;

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
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.domain.search.TwinflowSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.twinflow.TwinflowSchemaSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowSchemaSearchService {
    private final AuthService authService;
    private final TwinflowSchemaRepository twinflowSchemaRepository;

    public PaginationResult<TwinflowSchemaEntity> findTwinflowSchemaForDomain(TwinflowSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<TwinflowSchemaEntity> spec = createTwinflowSchemaSearchSpecification(search)
                .and(checkFieldUuid(domainId, PermissionGroupEntity.Fields.domainId));
        Page<TwinflowSchemaEntity> ret = twinflowSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowSchemaEntity> createTwinflowSchemaSearchSpecification(TwinflowSchemaSearch search) {
        return Specification.allOf(
                checkFieldLikeContainsIn(search.getNameLikeList(), false, true, TwinflowSchemaEntity.Fields.name),
                checkFieldLikeContainsIn(search.getNameNotLikeList(), true, true, TwinflowSchemaEntity.Fields.name),
                checkFieldLikeContainsIn(search.getDescriptionLikeList(), false, true, TwinflowSchemaEntity.Fields.description),
                checkFieldLikeContainsIn(search.getDescriptionNotLikeList(), true, true, TwinflowSchemaEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinflowSchemaEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, TwinflowSchemaEntity.Fields.id),
                checkUuidIn(search.getBusinessAccountIdList(), false, true, TwinflowSchemaEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, true, TwinflowSchemaEntity.Fields.businessAccountId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinflowSchemaEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, TwinflowSchemaEntity.Fields.createdByUserId));

    }
}
