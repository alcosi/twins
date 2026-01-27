package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassSchemaSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLocalDateTimeBetween;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassSchemaSearchService {
    private final TwinClassSchemaRepository twinClassSchemaRepository;
    private final AuthService authService;

    public PaginationResult<TwinClassSchemaEntity> findTwinClassSchemas(TwinClassSchemaSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassSchemaEntity> spec = createTwinClassSchemaSearchSpecification(search);
        Page<TwinClassSchemaEntity> ret = twinClassSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassSchemaEntity> createTwinClassSchemaSearchSpecification(TwinClassSchemaSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinClassSchemaEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinClassSchemaEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassSchemaEntity.Fields.id),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinClassSchemaEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinClassSchemaEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinClassSchemaEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinClassSchemaEntity.Fields.description),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinClassSchemaEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, false, TwinClassSchemaEntity.Fields.createdByUserId),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), TwinClassSchemaEntity.Fields.createdAt)
        );
    }
}