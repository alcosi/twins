package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class DomainBusinessAccountSearchService {
    private final DomainBusinessAccountRepository domainBusinessAccountRepository;
    private final AuthService authService;

    public Specification<DomainBusinessAccountEntity> createDomainBusinessAccountEntitySearchSpecification(DomainBusinessAccountSearch domainBusinessAccountSearch) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return checkUuid(DomainBusinessAccountEntity.Fields.domainId, domainId)
                .and(checkUuidIn(domainBusinessAccountSearch.getIdList(),false, false, DomainBusinessAccountEntity.Fields.id))
                .and(checkUuidIn(domainBusinessAccountSearch.getIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.id))
                .and(checkBusinessAccountFieldLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameLikeList(), false))
                .and(checkBusinessAccountFieldNotLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameNotLikeList(), true))
                .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdList(), false, false, DomainBusinessAccountEntity.Fields.businessAccountId))
                .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountEntity.Fields.businessAccountId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTierIdList(), false, false, DomainBusinessAccountEntity.Fields.tierId))
                .and(checkUuidIn(domainBusinessAccountSearch.getTierIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.tierId))
                .and(checkUuidIn(domainBusinessAccountSearch.getNotificationSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.notificationSchemaId))
                .and(checkUuidIn(domainBusinessAccountSearch.getNotificationSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.notificationSchemaId))
                .and(CommonSpecification.checkFieldIntegerRange(domainBusinessAccountSearch.getStorageUsedSizeRange(), DomainBusinessAccountEntity.Fields.attachmentsStorageUsedSize))
                .and(CommonSpecification.checkFieldIntegerRange(domainBusinessAccountSearch.getStorageUsedCountRange(), DomainBusinessAccountEntity.Fields.attachmentsStorageUsedCount))
                .and(CommonSpecification.checkFieldLocalDateTimeBetween(domainBusinessAccountSearch.getCreateAtRange(), DomainBusinessAccountEntity.Fields.createdAt));
    }

    public PaginationResult<DomainBusinessAccountEntity> findDomainBusinessAccounts(DomainBusinessAccountSearch domainBusinessAccountSearch, SimplePagination pagination) throws ServiceException {
        if (domainBusinessAccountSearch == null)
            domainBusinessAccountSearch = new DomainBusinessAccountSearch(); //no filters
        Page<DomainBusinessAccountEntity> domainBusinessAccountsList = domainBusinessAccountRepository
                .findAll(createDomainBusinessAccountEntitySearchSpecification(domainBusinessAccountSearch),
                        PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainBusinessAccountsList, pagination);
    }
}
