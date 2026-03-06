package org.twins.core.controller.rest.priv.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
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

    private final AuthService authService;

    public Specification<DomainBusinessAccountEntity> createDomainBusinessAccountEntitySearchSpecification(DomainBusinessAccountSearch domainBusinessAccountSearch) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return checkUuid(DomainBusinessAccountEntity.Fields.domainId, domainId)
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
}
