package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dao.trigger.TwinFactoryTriggerRepository;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinFactoryTriggerSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinFactoryTriggerGroupField;
import org.twins.core.enums.sort.TwinFactoryTriggerSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryTriggerSearchService extends EntitySearchService
        <TwinFactoryTriggerSearch, TwinFactoryTriggerEntity, TwinFactoryTriggerSortField, TwinFactoryTriggerGroupField> {
    private final TwinFactoryTriggerRepository twinFactoryTriggerRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryTriggerEntity> jpaSpecificationExecutor() {
        return twinFactoryTriggerRepository;
    }

    @Override
    public TwinFactoryTriggerSearch emptySearch() {
        return new TwinFactoryTriggerSearch();
    }

    @Override
    protected TwinFactoryTriggerEntity newEntity() {
        return new TwinFactoryTriggerEntity();
    }

    @Override
    protected Class<TwinFactoryTriggerEntity> entityClass() {
        return TwinFactoryTriggerEntity.class;
    }

    @Override
    public Specification<TwinFactoryTriggerEntity> createFilterSpecification(TwinFactoryTriggerSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuid(domainId, false, true, TwinFactoryTriggerEntity.Fields.twinFactorySpecOnly, TwinFactoryEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinFactoryIdList(), false, true, TwinFactoryTriggerEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, true, TwinFactoryTriggerEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getTwinTriggerIdList(), false, true, TwinFactoryTriggerEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.twinTriggerId),
                checkTernary(search.getActive(), TwinFactoryTriggerEntity.Fields.active),
                checkTernary(search.getAsync(), TwinFactoryTriggerEntity.Fields.async)
        );
    }

    @Override
    protected TwinFactoryTriggerSortField defaultSortField() {
        return TwinFactoryTriggerSortField.active;
    }

    @Override
    public Specification<TwinFactoryTriggerEntity> createSortSpecification(TwinFactoryTriggerSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.active);
            case description -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.description);
            case async -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.async);
            case twinFactoryConditionInvert -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.twinFactoryConditionInvert);
            case inputTwinClassName -> toSortSpecificationDirect(ascending, locale, TwinFactoryTriggerEntity.Fields.inputTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case twinFactoryName -> toSortSpecificationDirect(ascending, locale, TwinFactoryTriggerEntity.Fields.twinFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case twinFactoryConditionSetName -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.twinFactoryConditionSetSpecOnly, TwinFactoryConditionSetEntity.Fields.name);
            case twinTriggerName -> toSortSpecification(ascending, TwinFactoryTriggerEntity.Fields.twinTriggerSpecOnly, TwinTriggerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinFactoryTriggerGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinFactoryId -> TwinFactoryTriggerEntity.Fields.twinFactoryId;
            case inputTwinClassId -> TwinFactoryTriggerEntity.Fields.inputTwinClassId;
            case twinTriggerId -> TwinFactoryTriggerEntity.Fields.twinTriggerId;
            case active -> TwinFactoryTriggerEntity.Fields.active;
            case async -> TwinFactoryTriggerEntity.Fields.async;
            case twinFactoryConditionInvert -> TwinFactoryTriggerEntity.Fields.twinFactoryConditionInvert;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryTriggerEntity entity, TwinFactoryTriggerGroupField field, Object o) {
        switch (field) {
            case twinFactoryId -> entity.setTwinFactoryId((UUID) o);
            case inputTwinClassId -> entity.setInputTwinClassId((UUID) o);
            case twinTriggerId -> entity.setTwinTriggerId((UUID) o);
            case active -> entity.setActive((Boolean) o);
            case async -> entity.setAsync((Boolean) o);
            case twinFactoryConditionInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
        }
    }
}
