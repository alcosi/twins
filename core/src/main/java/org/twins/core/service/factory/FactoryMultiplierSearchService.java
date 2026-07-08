package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.FactoryMultiplierSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryMultiplierGroupField;
import org.twins.core.enums.sort.FactoryMultiplierSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierSpecification.checkDomainId;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryMultiplierSearchService extends EntitySearchService
        <FactoryMultiplierSearch, TwinFactoryMultiplierEntity, FactoryMultiplierSortField, FactoryMultiplierGroupField> {
    private final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryMultiplierEntity> jpaSpecificationExecutor() {
        return twinFactoryMultiplierRepository;
    }

    @Override
    public FactoryMultiplierSearch emptySearch() {
        return new FactoryMultiplierSearch();
    }

    @Override
    protected TwinFactoryMultiplierEntity newEntity() {
        return new TwinFactoryMultiplierEntity();
    }

    @Override
    protected Class<TwinFactoryMultiplierEntity> entityClass() {
        return TwinFactoryMultiplierEntity.class;
    }

    @Override
    public Specification<TwinFactoryMultiplierEntity> createFilterSpecification(FactoryMultiplierSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryMultiplierEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryMultiplierEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkIntegerIn(search.getMultiplierFeaturerIdList(), false, TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId),
                checkIntegerIn(search.getMultiplierFeaturerIdExcludeList(), true, TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryMultiplierEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryMultiplierEntity.Fields.description),
                checkTernary(search.getActive(), TwinFactoryMultiplierEntity.Fields.active)
        );
    }

    @Override
    public Specification<TwinFactoryMultiplierEntity> createSortSpecification(FactoryMultiplierSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryMultiplierSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryMultiplierEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryMultiplierEntity.Fields.description);
            case inputTwinClassName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryMultiplierEntity.Fields.inputTwinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryMultiplierEntity.Fields.twinFactory, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(FactoryMultiplierGroupField groupField) {
        return switch (groupField) {
            case factoryId -> TwinFactoryMultiplierEntity.Fields.twinFactoryId;
            case inputTwinClassId -> TwinFactoryMultiplierEntity.Fields.inputTwinClassId;
            case multiplierFeaturerId -> TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId;
            case active -> TwinFactoryMultiplierEntity.Fields.active;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryMultiplierEntity entity, FactoryMultiplierGroupField field, Object o) {
        switch (field) {
            case factoryId -> entity.setTwinFactoryId((UUID) o);
            case inputTwinClassId -> entity.setInputTwinClassId((UUID) o);
            case multiplierFeaturerId -> entity.setMultiplierFeaturerId((Integer) o);
            case active -> entity.setActive((Boolean) o);
        }
    }
}
