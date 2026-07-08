package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepRepository;
import org.twins.core.domain.search.FactoryPipelineStepSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryPipelineStepGroupField;
import org.twins.core.enums.sort.FactoryPipelineStepSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryPipelineStepSpecification.checkDomainId;
import static org.twins.core.dao.specifications.factory.FactoryPipelineStepSpecification.checkFactoryIdIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryPipelineStepSearchService extends EntitySearchService
        <FactoryPipelineStepSearch, TwinFactoryPipelineStepEntity, FactoryPipelineStepSortField, FactoryPipelineStepGroupField> {
    private final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryPipelineStepEntity> jpaSpecificationExecutor() {
        return twinFactoryPipelineStepRepository;
    }

    @Override
    public FactoryPipelineStepSearch emptySearch() {
        return new FactoryPipelineStepSearch();
    }

    @Override
    protected TwinFactoryPipelineStepEntity newEntity() {
        return new TwinFactoryPipelineStepEntity();
    }

    @Override
    protected Class<TwinFactoryPipelineStepEntity> entityClass() {
        return TwinFactoryPipelineStepEntity.class;
    }

    @Override
    public Specification<TwinFactoryPipelineStepEntity> createFilterSpecification(FactoryPipelineStepSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryPipelineStepEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineStepEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.id),
                checkFactoryIdIn(search.getFactoryIdList(), false),
                checkFactoryIdIn(search.getFactoryIdExcludeList(), true),
                checkUuidIn(search.getFactoryPipelineIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId),
                checkUuidIn(search.getFactoryPipelineIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId),
                checkIntegerIn(search.getFillerFeaturerIdList(), false, TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId),
                checkIntegerIn(search.getFillerFeaturerIdExcludeList(), true, TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId),
                checkTernary(search.getConditionInvert(), TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert),
                checkTernary(search.getActive(), TwinFactoryPipelineStepEntity.Fields.active),
                checkTernary(search.getOptional(), TwinFactoryPipelineStepEntity.Fields.optional)
        );
    }

    @Override
    public Specification<TwinFactoryPipelineStepEntity> createSortSpecification(FactoryPipelineStepSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryPipelineStepSortField.order;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case order ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.order);
            case active ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.active);
            case description ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.description);
            case optional ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.optional);
            case factoryConditionInvert ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert);
            case factoryConditionSetName ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSet, TwinFactoryConditionSetEntity.Fields.name);
            case fillerFeaturerName ->
                    toSortSpecification(ascending, TwinFactoryPipelineStepEntity.Fields.fillerFeaturerSpecOnly, FeaturerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryPipelineStepGroupField groupField) {
        return switch (groupField) {
            case factoryPipelineId -> TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId;
            case factoryConditionSetId -> TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId;
            case fillerFeaturerId -> TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId;
            case active -> TwinFactoryPipelineStepEntity.Fields.active;
            case optional -> TwinFactoryPipelineStepEntity.Fields.optional;
            case factoryConditionInvert -> TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryPipelineStepEntity entity, FactoryPipelineStepGroupField field, Object o) {
        switch (field) {
            case factoryPipelineId -> entity.setTwinFactoryPipelineId((UUID) o);
            case factoryConditionSetId -> entity.setTwinFactoryConditionSetId((UUID) o);
            case fillerFeaturerId -> entity.setFillerFeaturerId((Integer) o);
            case active -> entity.setActive((Boolean) o);
            case optional -> entity.setOptional((Boolean) o);
            case factoryConditionInvert -> entity.setTwinFactoryConditionInvert((Boolean) o);
        }
    }
}
