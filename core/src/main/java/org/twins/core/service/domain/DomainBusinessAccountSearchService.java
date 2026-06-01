package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountGroupField;
import org.twins.core.enums.sort.DomainBusinessAccountSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.*;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.checkUuid;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.toSortSpecification;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainBusinessAccountSearchService extends EntitySearchService
        <DomainBusinessAccountSearch, DomainBusinessAccountEntity, DomainBusinessAccountSortField, DomainBusinessAccountGroupField> {
    private final DomainBusinessAccountRepository domainBusinessAccountRepository;

    @Override
    public JpaSpecificationExecutor<DomainBusinessAccountEntity> jpaSpecificationExecutor() {
        return domainBusinessAccountRepository;
    }

    @Override
    public DomainBusinessAccountSearch emptySearch() {
        return new DomainBusinessAccountSearch();
    }

    @Override
    protected DomainBusinessAccountEntity newEntity() {
        return new DomainBusinessAccountEntity();
    }

    @Override
    protected Class<DomainBusinessAccountEntity> entityClass() {
        return DomainBusinessAccountEntity.class;
    }

    @Override
    public Specification<DomainBusinessAccountEntity> createFilterSpecification(DomainBusinessAccountSearch search, UUID domainId) {
        return Specification.allOf(
                checkUuid(DomainBusinessAccountEntity.Fields.domainId, domainId),
                checkUuidIn(search.getIdList(), false, false, DomainBusinessAccountEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.id),
                checkBusinessAccountFieldLikeIn(BusinessAccountEntity.Fields.name, search.getBusinessAccountNameLikeList(), false),
                checkBusinessAccountFieldNotLikeIn(BusinessAccountEntity.Fields.name, search.getBusinessAccountNameNotLikeList(), true),
                checkUuidIn(search.getPermissionSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getPermissionSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.permissionSchemaId),
                checkUuidIn(search.getTwinflowSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinflowSchemaId),
                checkUuidIn(search.getTwinflowSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinflowSchemaId),
                checkUuidIn(search.getTwinClassSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinClassSchemaId),
                checkUuidIn(search.getTwinClassSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinClassSchemaId),
                checkUuidIn(search.getBusinessAccountIdList(), false, false, DomainBusinessAccountEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountEntity.Fields.businessAccountId),
                checkUuidIn(search.getTierIdList(), false, false, DomainBusinessAccountEntity.Fields.tierId),
                checkUuidIn(search.getTierIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.tierId),
                checkUuidIn(search.getNotificationSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.notificationSchemaId),
                checkUuidIn(search.getNotificationSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.notificationSchemaId),
                checkFieldIntegerRange(search.getStorageUsedSizeRange(), DomainBusinessAccountEntity.Fields.attachmentsStorageUsedSize),
                checkFieldIntegerRange(search.getStorageUsedCountRange(), DomainBusinessAccountEntity.Fields.attachmentsStorageUsedCount),
                checkFieldLocalDateTimeBetween(search.getCreateAtRange(), DomainBusinessAccountEntity.Fields.createdAt)
        );
    }

    @Override
    public Specification<DomainBusinessAccountEntity> createSortSpecification(DomainBusinessAccountSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = DomainBusinessAccountSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.createdAt);
            case businessAccountName ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);
            case permissionSchemaName ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.name);
            case twinClassSchemaName ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.twinClassSchemaSpecOnly, TwinClassSchemaEntity.Fields.name);
            case twinflowSchemaName ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.twinflowSchemaSpecOnly, TwinflowSchemaEntity.Fields.name);
            case notificationSchemaName ->
                    I18nSpecification.toSortSpecification(ascending, locale, DomainBusinessAccountEntity.Fields.notificationSchemaSpecOnly, NotificationSchemaEntity.Fields.nameI18n);
            case tierName ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.tier, TierEntity.Fields.name);
            case attachmentsStorageUsedCount ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.attachmentsStorageUsedCount);
            case attachmentsStorageUsedSize ->
                    toSortSpecification(ascending, DomainBusinessAccountEntity.Fields.attachmentsStorageUsedSize);
        };
    }

    @Override
    public String convertToEntityField(DomainBusinessAccountGroupField groupField) {
        return switch (groupField) {
            case permissionSchemaId -> DomainBusinessAccountEntity.Fields.permissionSchemaId;
            case twinClassSchemaId -> DomainBusinessAccountEntity.Fields.twinClassSchemaId;
            case twinflowSchemaId -> DomainBusinessAccountEntity.Fields.twinflowSchemaId;
            case notificationSchemaId -> DomainBusinessAccountEntity.Fields.notificationSchemaId;
            case tierId -> DomainBusinessAccountEntity.Fields.tierId;
        };
    }

    @Override
    public void mapGroupedField(DomainBusinessAccountEntity entity, DomainBusinessAccountGroupField field, Object o) {
        switch (field) {
            case permissionSchemaId -> entity.setPermissionSchemaId((UUID) o);
            case twinClassSchemaId -> entity.setTwinClassSchemaId((UUID) o);
            case twinflowSchemaId -> entity.setTwinflowSchemaId((UUID) o);
            case notificationSchemaId -> entity.setNotificationSchemaId((UUID) o);
            case tierId -> entity.setTierId((UUID) o);
        }
    }
}
