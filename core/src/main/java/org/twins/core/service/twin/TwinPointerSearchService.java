package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerRepository;
import org.twins.core.domain.twin.TwinPointerSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinPointerGroupField;
import org.twins.core.enums.sort.TwinPointerSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinPointerSearchService extends EntitySearchService
        <TwinPointerSearch, TwinPointerEntity, TwinPointerSortField, TwinPointerGroupField> {
    private final TwinPointerRepository twinPointerRepository;

    @Override
    public JpaSpecificationExecutor<TwinPointerEntity> jpaSpecificationExecutor() {
        return twinPointerRepository;
    }

    @Override
    public TwinPointerSearch emptySearch() {
        return new TwinPointerSearch();
    }

    @Override
    protected TwinPointerEntity newEntity() {
        return new TwinPointerEntity();
    }

    @Override
    protected Class<TwinPointerEntity> entityClass() {
        return TwinPointerEntity.class;
    }

    @Override
    public Specification<TwinPointerEntity> createFilterSpecification(TwinPointerSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                // domain_id = current domain OR domain_id IS NULL: system/shared pointers (null domain)
                // are visible to every domain, they are just not editable (see TwinPointerService.updateTwinPointers)
                checkUuid(domainId, false, true, TwinPointerEntity.Fields.domainId),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinPointerEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinPointerEntity.Fields.name),
                checkUuidIn(search.getIdList(), false, false, TwinPointerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinPointerEntity.Fields.id),
                checkUuidIn(search.getTwinClassIdList(), false, true, TwinPointerEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, true, TwinPointerEntity.Fields.twinClassId),
                checkIntegerIn(search.getPointerFeaturerIdList(), false, TwinPointerEntity.Fields.pointerFeaturerId),
                checkIntegerIn(search.getPointerFeaturerIdExcludeList(), true, TwinPointerEntity.Fields.pointerFeaturerId)
        );
    }

    @Override
    public Specification<TwinPointerEntity> createSortSpecification(TwinPointerSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = TwinPointerSortField.name;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, TwinPointerEntity.Fields.name);
            case pointerFeaturerId -> toSortSpecification(ascending, TwinPointerEntity.Fields.pointerFeaturerId);
            case twinClassId -> toSortSpecification(ascending, TwinPointerEntity.Fields.twinClassId);
            case optional -> toSortSpecification(ascending, TwinPointerEntity.Fields.optional);
            case id -> toSortSpecification(ascending, TwinPointerEntity.Fields.id);
            case createdAt -> toSortSpecification(ascending, TwinPointerEntity.Fields.createdAt);
            case createdByUserId -> toSortSpecification(ascending, TwinPointerEntity.Fields.createdByUserId);
        };
    }

    @Override
    public String convertToEntityField(TwinPointerGroupField groupField) {
        return switch (groupField) {
            case twinClassId -> TwinPointerEntity.Fields.twinClassId;
            case pointerFeaturerId -> TwinPointerEntity.Fields.pointerFeaturerId;
            case createdByUserId -> TwinPointerEntity.Fields.createdByUserId;
            case optional -> TwinPointerEntity.Fields.optional;
        };
    }

    @Override
    public void mapGroupedField(TwinPointerEntity entity, TwinPointerGroupField field, Object o) {
        switch (field) {
            case twinClassId -> entity.setTwinClassId((UUID) o);
            case pointerFeaturerId -> entity.setPointerFeaturerId((Integer) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
            case optional -> entity.setOptional((Boolean) o);
        }
    }
}
