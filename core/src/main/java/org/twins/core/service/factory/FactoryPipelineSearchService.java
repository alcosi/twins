package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.FactoryPipelineSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryPipelineGroupField;
import org.twins.core.enums.sort.FactoryPipelineSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryPipelineSpecification.checkDomainId;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryPipelineSearchService extends EntitySearchService
        <FactoryPipelineSearch, TwinFactoryPipelineEntity, FactoryPipelineSortField, FactoryPipelineGroupField> {
    private final TwinFactoryPipelineRepository twinFactoryPipelineRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryPipelineEntity> jpaSpecificationExecutor() {
        return twinFactoryPipelineRepository;
    }

    @Override
    public FactoryPipelineSearch emptySearch() {
        return new FactoryPipelineSearch();
    }

    @Override
    protected TwinFactoryPipelineEntity newEntity() {
        return new TwinFactoryPipelineEntity();
    }

    @Override
    protected Class<TwinFactoryPipelineEntity> entityClass() {
        return TwinFactoryPipelineEntity.class;
    }

    @Override
    public Specification<TwinFactoryPipelineEntity> createFilterSpecification(FactoryPipelineSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryPipelineEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryPipelineEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryPipelineEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryPipelineEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getOutputTwinStatusIdList(), false, false, TwinFactoryPipelineEntity.Fields.outputTwinStatusId),
                checkUuidIn(search.getOutputTwinStatusIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.outputTwinStatusId),
                checkUuidIn(search.getNextFactoryIdList(), false, false, TwinFactoryPipelineEntity.Fields.nextTwinFactoryId),
                checkUuidIn(search.getNextFactoryIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.nextTwinFactoryId),
                checkTernary(search.getActive(), TwinFactoryPipelineEntity.Fields.active),
                checkTernary(search.getNextFactoryLimitScope(), TwinFactoryPipelineEntity.Fields.nextTwinFactoryLimitScope)
        );
    }

    @Override
    public Specification<TwinFactoryPipelineEntity> createSortSpecification(FactoryPipelineSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryPipelineSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryPipelineEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryPipelineEntity.Fields.description);
            case factoryConditionSetInvert ->
                    toSortSpecification(ascending, TwinFactoryPipelineEntity.Fields.twinFactoryConditionInvert);
            case nextFactoryLimitScope ->
                    toSortSpecification(ascending, TwinFactoryPipelineEntity.Fields.nextTwinFactoryLimitScope);
            case inputTwinClassName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryPipelineEntity.Fields.inputTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case outputTwinStatusName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryPipelineEntity.Fields.outputTwinStatusSpecOnly, TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryPipelineEntity.Fields.twinFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case nextFactoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryPipelineEntity.Fields.nextTwinFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryConditionSetName ->
                    toSortSpecification(ascending, TwinFactoryPipelineEntity.Fields.conditionSetSpecOnly, TwinFactoryConditionSetEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryPipelineGroupField groupField) {
        return switch (groupField) {
            case factoryId -> TwinFactoryPipelineEntity.Fields.twinFactoryId;
            case inputTwinClassId -> TwinFactoryPipelineEntity.Fields.inputTwinClassId;
            case factoryConditionSetId -> TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId;
            case outputTwinStatusId -> TwinFactoryPipelineEntity.Fields.outputTwinStatusId;
            case nextFactoryId -> TwinFactoryPipelineEntity.Fields.nextTwinFactoryId;
            case active -> TwinFactoryPipelineEntity.Fields.active;
            case nextFactoryLimitScope -> TwinFactoryPipelineEntity.Fields.nextTwinFactoryLimitScope;
            case factoryConditionSetInvert -> TwinFactoryPipelineEntity.Fields.twinFactoryConditionInvert;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryPipelineEntity entity, FactoryPipelineGroupField field, Object o) {
        switch (field) {
            case factoryId -> entity.setTwinFactoryId((UUID) o);
            case inputTwinClassId -> entity.setInputTwinClassId((UUID) o);
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case outputTwinStatusId -> entity.setOutputTwinStatusId((UUID) o);
            case nextFactoryId -> entity.setNextTwinFactoryId((UUID) o);
            case active -> entity.setActive((Boolean) o);
            case nextFactoryLimitScope -> entity.setNextTwinFactoryLimitScope((Boolean) o);
            case factoryConditionSetInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
        }
    }
}
