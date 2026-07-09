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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryEraserRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.FactoryEraserSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryEraserGroupField;
import org.twins.core.enums.sort.FactoryEraserSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.cambium.common.util.EnumUtils.convertOrEmpty;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryEraserSpecification.checkDomainId;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryEraserSearchService extends EntitySearchService
        <FactoryEraserSearch, TwinFactoryEraserEntity, FactoryEraserSortField, FactoryEraserGroupField> {
    private final TwinFactoryEraserRepository twinFactoryEraserRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryEraserEntity> jpaSpecificationExecutor() {
        return twinFactoryEraserRepository;
    }

    @Override
    public FactoryEraserSearch emptySearch() {
        return new FactoryEraserSearch();
    }

    @Override
    protected TwinFactoryEraserEntity newEntity() {
        return new TwinFactoryEraserEntity();
    }

    @Override
    protected Class<TwinFactoryEraserEntity> entityClass() {
        return TwinFactoryEraserEntity.class;
    }

    @Override
    public Specification<TwinFactoryEraserEntity> createFilterSpecification(FactoryEraserSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryEraserEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryEraserEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryEraserEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryEraserEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryEraserEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId),
                checkFieldLikeIn(convertOrEmpty(search.getEraseActionLikeList()), false, true, TwinFactoryEraserEntity.Fields.eraserAction),
                checkFieldLikeIn(convertOrEmpty(search.getEraseActionNotLikeList()), true, true, TwinFactoryEraserEntity.Fields.eraserAction),
                checkTernary(search.getConditionInvert(), TwinFactoryEraserEntity.Fields.twinFactoryConditionInvert),
                checkTernary(search.getActive(), TwinFactoryEraserEntity.Fields.active)
        );
    }

    @Override
    public Specification<TwinFactoryEraserEntity> createSortSpecification(FactoryEraserSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryEraserSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryEraserEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryEraserEntity.Fields.description);
            case factoryConditionSetInvert ->
                    toSortSpecification(ascending, TwinFactoryEraserEntity.Fields.twinFactoryConditionInvert);
            case action ->
                    toSortSpecification(ascending, TwinFactoryEraserEntity.Fields.eraserAction);
            case inputTwinClassName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryEraserEntity.Fields.inputTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryEraserEntity.Fields.twinFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryConditionSetName ->
                    toSortSpecification(ascending, TwinFactoryEraserEntity.Fields.conditionSetSpecOnly, TwinFactoryConditionSetEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryEraserGroupField groupField) {
        return switch (groupField) {
            case factoryId -> TwinFactoryEraserEntity.Fields.twinFactoryId;
            case inputTwinClassId -> TwinFactoryEraserEntity.Fields.inputTwinClassId;
            case factoryConditionSetId -> TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId;
            case factoryConditionSetInvert -> TwinFactoryEraserEntity.Fields.twinFactoryConditionInvert;
            case active -> TwinFactoryEraserEntity.Fields.active;
            case action -> TwinFactoryEraserEntity.Fields.eraserAction;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryEraserEntity entity, FactoryEraserGroupField field, Object o) {
        switch (field) {
            case factoryId -> entity.setTwinFactoryId((UUID) o);
            case inputTwinClassId -> entity.setInputTwinClassId((UUID) o);
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case factoryConditionSetInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
            case active -> entity.setActive((Boolean) o);
            case action -> entity.setEraserAction((org.twins.core.enums.factory.FactoryEraserAction) o);
        }
    }
}
