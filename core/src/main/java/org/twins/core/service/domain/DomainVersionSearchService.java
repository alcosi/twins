package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.domain.DomainVersionRepository;
import org.twins.core.domain.search.DomainVersionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.domain.DomainVersionStatus;
import org.twins.core.enums.sort.DomainVersionGroupField;
import org.twins.core.enums.sort.DomainVersionSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainVersionSearchService extends EntitySearchService
        <DomainVersionSearch, DomainVersionEntity, DomainVersionSortField, DomainVersionGroupField> {
    private final DomainVersionRepository domainVersionRepository;

    @Override
    public JpaSpecificationExecutor<DomainVersionEntity> jpaSpecificationExecutor() {
        return domainVersionRepository;
    }

    @Override
    public DomainVersionSearch emptySearch() {
        return new DomainVersionSearch();
    }

    @Override
    protected DomainVersionEntity newEntity() {
        return new DomainVersionEntity();
    }

    @Override
    protected Class<DomainVersionEntity> entityClass() {
        return DomainVersionEntity.class;
    }

    @Override
    public Specification<DomainVersionEntity> createFilterSpecification(DomainVersionSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                // domain_id is NOT NULL here (no system rows) — strict equality
                checkUuid(domainId, false, false, DomainVersionEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, DomainVersionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DomainVersionEntity.Fields.id),
                checkFieldLikeIn(search.getVersionLikeList(), false, true, DomainVersionEntity.Fields.version),
                checkFieldLikeIn(search.getVersionNotLikeList(), true, true, DomainVersionEntity.Fields.version),
                checkFieldLikeIn(search.getNameLikeList(), false, true, DomainVersionEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, DomainVersionEntity.Fields.name),
                checkFieldIn(search.getStatusList(), false, DomainVersionEntity.Fields.status),
                checkFieldIn(search.getStatusExcludeList(), true, DomainVersionEntity.Fields.status),
                checkUuidIn(search.getPreviousDomainVersionIdList(), false, false, DomainVersionEntity.Fields.previousDomainVersionId),
                checkUuidIn(search.getPreviousDomainVersionIdExcludeList(), true, false, DomainVersionEntity.Fields.previousDomainVersionId),
                checkFieldLocalDateTimeBetween(search.getStartedAt(), DomainVersionEntity.Fields.startedAt),
                checkFieldLocalDateTimeBetween(search.getReleasedAt(), DomainVersionEntity.Fields.releasedAt)
        );
    }

    @Override
    protected DomainVersionSortField defaultSortField() {
        return DomainVersionSortField.startedAt;
    }

    @Override
    public Specification<DomainVersionEntity> createSortSpecification(DomainVersionSortField sortField, SortDirection sortDirection, Locale locale) {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case version -> toSortSpecification(ascending, DomainVersionEntity.Fields.version);
            case name -> toSortSpecification(ascending, DomainVersionEntity.Fields.name);
            case status -> toSortSpecification(ascending, DomainVersionEntity.Fields.status);
            case order -> toSortSpecification(ascending, DomainVersionEntity.Fields.order);
            case startedAt -> toSortSpecification(ascending, DomainVersionEntity.Fields.startedAt);
            case releasedAt -> toSortSpecification(ascending, DomainVersionEntity.Fields.releasedAt);
            case id -> toSortSpecification(ascending, DomainVersionEntity.Fields.id);
        };
    }

    @Override
    public String convertToEntityField(DomainVersionGroupField groupField) {
        return switch (groupField) {
            case status -> DomainVersionEntity.Fields.status;
        };
    }

    @Override
    public void mapGroupedField(DomainVersionEntity entity, DomainVersionGroupField field, Object o) {
        switch (field) {
            case status -> entity.setStatus((DomainVersionStatus) o);
        }
    }
}
