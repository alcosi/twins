package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionRepository;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.domain.search.FactoryConditionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryConditionGroupField;
import org.twins.core.enums.sort.FactoryConditionSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryConditionSpecification.checkFieldConditionerFeaturerIdIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryConditionSearchService extends EntitySearchService
        <FactoryConditionSearch, TwinFactoryConditionEntity, FactoryConditionSortField, FactoryConditionGroupField> {
    private final TwinFactoryConditionRepository twinFactoryConditionRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryConditionEntity> jpaSpecificationExecutor() {
        return twinFactoryConditionRepository;
    }

    @Override
    public FactoryConditionSearch emptySearch() {
        return new FactoryConditionSearch();
    }

    @Override
    protected TwinFactoryConditionEntity newEntity() {
        return new TwinFactoryConditionEntity();
    }

    @Override
    protected Class<TwinFactoryConditionEntity> entityClass() {
        return TwinFactoryConditionEntity.class;
    }

    @Override
    public Specification<TwinFactoryConditionEntity> createFilterSpecification(FactoryConditionSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinFactoryConditionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryConditionEntity.Fields.id),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, false, TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId),
                checkFieldConditionerFeaturerIdIn(search.getConditionerFeaturerIdList(), false),
                checkFieldConditionerFeaturerIdIn(search.getConditionerFeaturerIdExcludeList(), true),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryConditionEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryConditionEntity.Fields.description),
                checkTernary(search.getInvert(), TwinFactoryConditionEntity.Fields.invert),
                checkTernary(search.getActive(), TwinFactoryConditionEntity.Fields.active)
        );
    }

    @Override
    public Specification<TwinFactoryConditionEntity> createSortSpecification(FactoryConditionSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryConditionSortField.active;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case active ->
                    toSortSpecification(ascending, TwinFactoryConditionEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryConditionEntity.Fields.description);
            case invert ->
                    toSortSpecification(ascending, TwinFactoryConditionEntity.Fields.invert);
            case factoryConditionSetName ->
                    toSortSpecification(ascending, TwinFactoryConditionEntity.Fields.conditionSet, TwinFactoryConditionSetEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryConditionGroupField groupField) {
        return switch (groupField) {
            case factoryConditionSetId -> TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId;
            case conditionerFeaturerId -> TwinFactoryConditionEntity.Fields.conditionerFeaturerId;
            case invert -> TwinFactoryConditionEntity.Fields.invert;
            case active -> TwinFactoryConditionEntity.Fields.active;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryConditionEntity entity, FactoryConditionGroupField field, Object o) {
        switch (field) {
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case conditionerFeaturerId -> entity.setConditionerFeaturerId((Integer) o);
            case invert -> entity.setInvert((Boolean) o);
            case active -> entity.setActive((Boolean) o);
        }
    }
}
