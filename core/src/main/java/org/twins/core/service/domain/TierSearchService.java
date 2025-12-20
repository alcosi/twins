package org.twins.core.service.domain;

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
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.domain.TierRepository;
import org.twins.core.domain.search.TierSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.domain.TierSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TierSearchService {
    private final TierRepository tierRepository;
    private final AuthService authService;

    public PaginationResult<TierEntity> findTiers(TierSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TierEntity> spec = createTierSearchSpecification(search);
        Page<TierEntity> ret = tierRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TierEntity> createTierSearchSpecification(TierSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkUuidIn(search.getIdList(), false, false, TierEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TierEntity.Fields.id),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, TierEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, false, TierEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getTwinflowSchemaIdList(), false, false, TierEntity.Fields.twinflowSchemaId),
                checkUuidIn(search.getTwinflowSchemaIdExcludeList(), true, false, TierEntity.Fields.twinflowSchemaId),
                checkUuidIn(search.getTwinclassSchemaIdList(), false, false, TierEntity.Fields.twinClassSchemaId),
                checkUuidIn(search.getTwinclassSchemaIdExcludeList(), true, false, TierEntity.Fields.twinClassSchemaId),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TierEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TierEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TierEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TierEntity.Fields.description),
                checkFieldLongRange(search.getAttachmentsStorageQuotaCountRange(), TierEntity.Fields.attachmentsStorageQuotaCount),
                checkFieldLongRange(search.getAttachmentsStorageQuotaSizeRange(), TierEntity.Fields.attachmentsStorageQuotaSize),
                checkFieldLongRange(search.getUserCountQuotaRange(), TierEntity.Fields.userCountQuota),
                checkTernary(search.getCustom(), TierEntity.Fields.custom)
        );
    }
}
