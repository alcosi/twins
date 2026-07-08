package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.FactoryMultiplierFilterSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryMultiplierFilterGroupField;
import org.twins.core.enums.sort.FactoryMultiplierFilterSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierFilterSpecification.checkDomainId;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierFilterSpecification.checkFactoryIdIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryMultiplierFilterSearchService extends EntitySearchService
        <FactoryMultiplierFilterSearch, TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterSortField, FactoryMultiplierFilterGroupField> {
    private final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryMultiplierFilterEntity> jpaSpecificationExecutor() {
        return twinFactoryMultiplierFilterRepository;
    }

    @Override
    public FactoryMultiplierFilterSearch emptySearch() {
        return new FactoryMultiplierFilterSearch();
    }

    @Override
    protected TwinFactoryMultiplierFilterEntity newEntity() {
        return new TwinFactoryMultiplierFilterEntity();
    }

    @Override
    protected Class<TwinFactoryMultiplierFilterEntity> entityClass() {
        return TwinFactoryMultiplierFilterEntity.class;
    }

    @Override
    public Specification<TwinFactoryMultiplierFilterEntity> createFilterSpecification(FactoryMultiplierFilterSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryMultiplierFilterEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryMultiplierFilterEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.id),
                checkFactoryIdIn(search.getFactoryIdList(), false),
                checkFactoryIdIn(search.getFactoryIdExcludeList(), true),
                checkUuidIn(search.getFactoryMultiplierIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId),
                checkUuidIn(search.getFactoryMultiplierIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId),
                checkTernary(search.getActive(), TwinFactoryMultiplierFilterEntity.Fields.active),
                checkTernary(search.getFactoryConditionInvert(), TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionInvert)
        );
    }

    @Override
    public Specification<TwinFactoryMultiplierFilterEntity> createSortSpecification(FactoryMultiplierFilterSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryMultiplierFilterSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryMultiplierFilterEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryMultiplierFilterEntity.Fields.description);
            case factoryConditionSetInvert ->
                    toSortSpecification(ascending, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionInvert);
            case inputTwinClassName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryMultiplierFilterEntity.Fields.inputTwinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryConditionSetName ->
                    toSortSpecification(ascending, TwinFactoryMultiplierFilterEntity.Fields.conditionSet, TwinFactoryConditionSetEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryMultiplierFilterGroupField groupField) {
        return switch (groupField) {
            case factoryMultiplierId -> TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId;
            case inputTwinClassId -> TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId;
            case factoryConditionSetId -> TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId;
            case active -> TwinFactoryMultiplierFilterEntity.Fields.active;
            case factoryConditionSetInvert -> TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionInvert;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryMultiplierFilterEntity entity, FactoryMultiplierFilterGroupField field, Object o) {
        switch (field) {
            case factoryMultiplierId -> entity.setTwinFactoryMultiplierId((UUID) o);
            case inputTwinClassId -> entity.setInputTwinClassId((UUID) o);
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case active -> entity.setActive((Boolean) o);
            case factoryConditionSetInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
        }
    }
}
