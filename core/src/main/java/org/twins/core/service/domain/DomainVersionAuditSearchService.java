package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.dao.domain.DomainVersionAuditRepository;
import org.twins.core.domain.search.DomainVersionAuditSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;
import org.twins.core.enums.domain.DomainVersionedTable;
import org.twins.core.enums.sort.DomainVersionAuditGroupField;
import org.twins.core.enums.sort.DomainVersionAuditSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainVersionAuditSearchService extends EntitySearchService
        <DomainVersionAuditSearch, DomainVersionAuditEntity, DomainVersionAuditSortField, DomainVersionAuditGroupField> {
    private final DomainVersionAuditRepository domainVersionAuditRepository;

    @Override
    public JpaSpecificationExecutor<DomainVersionAuditEntity> jpaSpecificationExecutor() {
        return domainVersionAuditRepository;
    }

    @Override
    public DomainVersionAuditSearch emptySearch() {
        return new DomainVersionAuditSearch();
    }

    @Override
    protected DomainVersionAuditEntity newEntity() {
        return new DomainVersionAuditEntity();
    }

    @Override
    protected Class<DomainVersionAuditEntity> entityClass() {
        return DomainVersionAuditEntity.class;
    }

    @Override
    public Specification<DomainVersionAuditEntity> createFilterSpecification(DomainVersionAuditSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                // domain_id is NOT NULL here (no system rows) — strict equality
                checkUuid(domainId, false, false, DomainVersionAuditEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, DomainVersionAuditEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DomainVersionAuditEntity.Fields.id),
                checkUuidIn(search.getDomainVersionIdList(), false, false, DomainVersionAuditEntity.Fields.domainVersionId),
                checkUuidIn(search.getDomainVersionIdExcludeList(), true, false, DomainVersionAuditEntity.Fields.domainVersionId),
                checkFieldIn(tableNames(search.getTableNameList()), false, DomainVersionAuditEntity.Fields.tableName),
                checkFieldIn(tableNames(search.getTableNameExcludeList()), true, DomainVersionAuditEntity.Fields.tableName),
                checkUuidIn(search.getRowIdList(), false, false, DomainVersionAuditEntity.Fields.rowId),
                checkUuidIn(search.getRowIdExcludeList(), true, false, DomainVersionAuditEntity.Fields.rowId),
                checkFieldIn(search.getOperationList(), false, DomainVersionAuditEntity.Fields.operation),
                checkFieldIn(search.getOperationExcludeList(), true, DomainVersionAuditEntity.Fields.operation),
                checkFieldIn(search.getDomainOperationStatusList(), false, DomainVersionAuditEntity.Fields.domainOperationStatus),
                checkFieldIn(search.getDomainOperationStatusExcludeList(), true, DomainVersionAuditEntity.Fields.domainOperationStatus),
                checkUuidIn(search.getActorUserIdList(), false, false, DomainVersionAuditEntity.Fields.actorUserId),
                checkUuidIn(search.getActorUserIdExcludeList(), true, false, DomainVersionAuditEntity.Fields.actorUserId),
                checkFieldLocalDateTimeBetween(search.getChangedAt(), DomainVersionAuditEntity.Fields.changedAt)
        );
    }

    @Override
    protected DomainVersionAuditSortField defaultSortField() {
        return DomainVersionAuditSortField.seq;
    }

    @Override
    public Specification<DomainVersionAuditEntity> createSortSpecification(DomainVersionAuditSortField sortField, SortDirection sortDirection, Locale locale) {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case seq -> toSortSpecification(ascending, DomainVersionAuditEntity.Fields.seq);
            case changedAt -> toSortSpecification(ascending, DomainVersionAuditEntity.Fields.changedAt);
            case tableName -> toSortSpecification(ascending, DomainVersionAuditEntity.Fields.tableName);
            case operation -> toSortSpecification(ascending, DomainVersionAuditEntity.Fields.operation);
            case id -> toSortSpecification(ascending, DomainVersionAuditEntity.Fields.id);
        };
    }

    @Override
    public String convertToEntityField(DomainVersionAuditGroupField groupField) {
        return switch (groupField) {
            case domainVersionId -> DomainVersionAuditEntity.Fields.domainVersionId;
            case tableName -> DomainVersionAuditEntity.Fields.tableName;
            case operation -> DomainVersionAuditEntity.Fields.operation;
            case domainOperationStatus -> DomainVersionAuditEntity.Fields.domainOperationStatus;
            case actorUserId -> DomainVersionAuditEntity.Fields.actorUserId;
        };
    }

    @Override
    public void mapGroupedField(DomainVersionAuditEntity entity, DomainVersionAuditGroupField field, Object o) {
        switch (field) {
            case domainVersionId -> entity.setDomainVersionId((UUID) o);
            case tableName -> entity.setTableName((String) o);
            case operation -> entity.setOperation((DomainVersionAuditOperation) o);
            case domainOperationStatus -> entity.setDomainOperationStatus((DomainOperationStatus) o);
            case actorUserId -> entity.setActorUserId((UUID) o);
        }
    }

    // API speaks the DomainVersionedTable alias, the audit column stores the physical name
    private static Set<String> tableNames(Set<DomainVersionedTable> tables) {
        return tables == null ? null : tables.stream().map(DomainVersionedTable::getTableName).collect(Collectors.toSet());
    }
}
