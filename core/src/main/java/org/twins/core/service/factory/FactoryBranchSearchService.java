package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.search.FactoryBranchSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryBranchGroupField;
import org.twins.core.enums.sort.FactoryBranchSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryBranchSpecification.checkDomainId;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryBranchSearchService extends EntitySearchService
        <FactoryBranchSearch, TwinFactoryBranchEntity, FactoryBranchSortField, FactoryBranchGroupField> {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryBranchEntity> jpaSpecificationExecutor() {
        return twinFactoryBranchRepository;
    }

    @Override
    public FactoryBranchSearch emptySearch() {
        return new FactoryBranchSearch();
    }

    @Override
    protected TwinFactoryBranchEntity newEntity() {
        return new TwinFactoryBranchEntity();
    }

    @Override
    protected Class<TwinFactoryBranchEntity> entityClass() {
        return TwinFactoryBranchEntity.class;
    }

    @Override
    public Specification<TwinFactoryBranchEntity> createFilterSpecification(FactoryBranchSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryBranchEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryBranchEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryBranchEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryBranchEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getNextFactoryIdList(), false, false, TwinFactoryBranchEntity.Fields.nextTwinFactoryId),
                checkUuidIn(search.getNextFactoryIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.nextTwinFactoryId),
                checkTernary(search.getConditionInvert(), TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert),
                checkTernary(search.getActive(), TwinFactoryBranchEntity.Fields.active)
        );
    }

    @Override
    public Specification<TwinFactoryBranchEntity> createSortSpecification(FactoryBranchSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryBranchSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryBranchEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryBranchEntity.Fields.description);
            case factoryConditionSetInvert ->
                    toSortSpecification(ascending, TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert);
            case factoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryBranchEntity.Fields.factorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case nextFactoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryBranchEntity.Fields.nextFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryConditionName ->
                    toSortSpecification(ascending, TwinFactoryBranchEntity.Fields.conditionSetSpecOnly, TwinFactoryConditionSetEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryBranchGroupField groupField) {
        return switch (groupField) {
            case factoryId -> TwinFactoryBranchEntity.Fields.twinFactoryId;
            case factoryConditionSetId -> TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId;
            case nextFactoryId -> TwinFactoryBranchEntity.Fields.nextTwinFactoryId;
            case active -> TwinFactoryBranchEntity.Fields.active;
            case factoryConditionSetInvert -> TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryBranchEntity entity, FactoryBranchGroupField field, Object o) {
        switch (field) {
            case factoryId -> entity.setTwinFactoryId((UUID) o);
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case nextFactoryId -> entity.setNextTwinFactoryId((UUID) o);
            case active -> entity.setActive((Boolean) o);
            case factoryConditionSetInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
        }
    }
}
